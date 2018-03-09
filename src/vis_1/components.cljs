(ns vis-1.components
  (:require
    [vis-1.utils :as utils :refer [update-network]]
    [taoensso.timbre :as timbre :refer [info]]
    [cemerick.url :refer [url url-encode]]
    [vis-1.state :refer [app-state node-state]]
    [vis-1.handler :refer [child-handler root-handler get-resource]]
    [cljs-react-material-ui.rum :as ui]
    [cljs-react-material-ui.icons :as icons]
    [rum.core :as rum :refer [defc defcs]]
    [cljs.pprint :as pprint]
    [cljs.tools.reader :as reader]
    [clojure.string :as str]
    [cljs-react-material-ui.core :refer [get-mui-theme color]]
    [reforms.rum :include-macros true :as f]
    [cljsjs.vis]))

(declare jet-icon)


;(defn create-table
;  [table-name table-state]
;  (info "Calling create-table")
;  (info "table-state" table-state)
;  (let [header (spy :info (mapv name (keys (if (vector? table-state) (first table-state) table-state))))]
;    [:div.container
;     [:div {:id table-name}
;      [:div.row
;       [:div.section.col.l12 [:h5 table-name]]]
;      (ui/table {:id           table-name
;                 :selectable   true
;                 :fixed-header true}
;                (ui/table-header {:display-select-all  false
;                                  :adjust-for-checkbox true}
;                                 (ui/table-row
;                                   (map #(ui/table-header-column (str/capitalize %)) (spy :info header))))
;                (ui/table-body {:show-row-hover true
;                                :striped-rows   true}
;                               (map
;                                 (fn [row]
;                                   (let [v (vals row)]
;                                     (ui/table-row
;                                       (map
;                                         #(cond
;                                           (map? %)  (if-not (and (empty? %) (> 1 (count %))) (create-table "Nested" %))
;                                           :default  (ui/table-row-column %)) (spy :info v)))))
;                                 table-state)))]]))



;(defcs task-table < rum/reactive
;                    (api/ajax-mixin
;                      (str/join "/" [api/rhdapi "workflows/tasks"])
;                      :get
;                      {}
;                      ::tasks)
;  [state]
;  (let [state @(::tasks state)
;        skeys [:friendlyName :injectableName :implementsTask]
;        records (mapv #(select-keys % skeys) state)
;        _ (println (first records))]
;    (create-table "Tasks" records)))
;
;
;(defcs workflows-table < rum/reactive
;                         (api/ajax-mixin
;                           (str/join "/" [api/rhdapi "workflows/graphs"])
;                           :get
;                           {}
;                           ::workflows)
;  [state]
;  (let [state @(::workflows state)
;        skeys [:friendlyName :injectableName :tasks :options]
;        records (mapv #(select-keys % skeys) state)]
;
;    (create-table "Workflows" records)))



(defn remove-indexes-atom [indexes a]
  (let [coll @a]
    (->> coll
         (keep-indexed vector)
         (remove (comp (set indexes) first))
         (mapv second)
         (reset! a))))


(defn remove-indexes-map [indexes a]
  (->> indexes
       (keep-indexed vector)
       (remove (comp (set indexes) first))
       (mapv second)))

(defn get-element-from-index [indexes a]
  (let [coll @a]
    (->> coll
         (keep-indexed vector)
         (remove (comp (set indexes) first))
         (mapv second)
         (reset! a))))

(defn update-row-select [event form-state]
  (let [row-num (->> event (.slice) js->clj first)
        _ (print "Row Selected " row-num)]
    (swap! form-state assoc :row-selected (int row-num))))

;
;(defc splash [app-state]
;  [:div {:style {:padding          "72px 24px"
;                 :background-color "#0296e5"}}
;   [:div {:style {
;                  :margin     "32px auto 0px"
;                  :text-align "center"
;                  :max-width  "575px"}}
;    [:h1 {:style {:color       "white"
;                  :font-weight 300
;                  :font-size   "56px"}}
;     "Server Automation Example"]]])


;(defn check-login [m app-state]
;  (let [accept-login (rum/cursor-in app-state [:accept-login])
;        user (:login m)
;        password (:password m)]
;    (if (and (= user "admin")
;             (= password "password"))
;      (rum/mount (home-page app-state) (js/document.getElementById "app"))
;      (js/alert "Failed login"))))



;(defc workflow-alerts
;  [alerts nodeid]
;  [:div
;   (ui/badge
;     {:badge-content alerts
;      :primary       true}
;     (ui/icon-button {:on-click #(api/post-cancel-workflow nodeid)}
;                     (icons/social-notifications)))])

;(rum/defcs login < rum/reactive
;                   (rum/local {} ::login-state)
;  [state app-state]
;  (let [login-state (::login-state state)]
;    [:div {:style {:position         "fixed"
;                   :background-color "#0296e5"
;                   :width            "100%"
;                   :height           "100%"}}
;     [:div {:style {
;                    :margin     "32px auto 0px"
;                    :text-align "center"
;                    :max-width  "575px"}}
;      [:h1 {:style {:color "white"}}
;       "RackHD"]
;      (jet)]
;     [:div.center-div {:id "login"}
;      (f/form {:on-submit #(check-login @login-state app-state)}
;              (f/text {:key "login"}
;                      "Login" login-state [:login])
;              (f/password {:key "password"} "Password" login-state [:password]))]]))

(declare inspector)

(defc app-bar < rum/reactive
  [app-state]
  (let [dialog-state (rum/cursor app-state :dialog-state)
        page-state (rum/cursor app-state :page-state)
        inspect-state (rum/cursor app-state :inspect-state)
        debug-state (rum/cursor app-state :debug-state)
        mock-state (rum/cursor app-state :mock)
        nodes (rum/cursor node-state :nodes)
        edges (rum/cursor node-state :edges)
        selected (rum/cursor app-state :selected)
        url (rum/cursor app-state :url)


        drawer-state (rum/cursor-in app-state [:drawer-state])]

    (rum/react drawer-state)
    (ui/app-bar {:title                         "RedFish Inspector"
                 :on-change                     #(print "changed")
                 :on-left-icon-button-touch-tap #(reset! drawer-state (not @drawer-state))}
                (ui/icon-button {:on-click (fn []
                                             (reset! nodes [])
                                             (reset! edges [])
                                             (reset! selected [])
                                             (reset! url []))}
                                ;(reset! debug-state (not @debug-state)))}
                                (if (rum/react node-state)
                                  (icons/action-info {:color "white"})
                                  (icons/action-info-outline {:color "white"})))

                (ui/icon-button {:on-click #(reset! inspect-state (not @inspect-state))}
                                (jet-icon)))))


(defc app-menu < rum/reactive
  [app-state]
  (let [drawer-state (rum/cursor-in app-state [:drawer-state])
        page-state (rum/cursor-in app-state [:page-state])
        inspect-state (rum/cursor-in page-state [:inspect])]
    (rum/react app-state)
    (ui/drawer {:container-style {:background-color "#524a4a"
                                  :top              "64px"
                                  :color            "#fff"}
                :docked          false
                :open            (rum/react drawer-state)}
               ;(ui/menu-item  {:on-change (fn [r]
               ;                              (reset! drawer-state false))}
               ;   "foo")
               (ui/menu-item {:style    {:color "white"}
                              :on-click (fn []
                                          ;(reset! form-data (empty {}))
                                          (reset! drawer-state false)
                                          (reset! page-state :main))}
                             "Main"))))
;
;(ui/menu-item {:style    {:color "white"}
;               :on-click (fn []
;                           ;(reset! form-data (empty {}))
;                           (reset! drawer-state false)
;                           (reset! page-state :splash))}
;              "Home")
;(ui/menu-item {:style    {:color "white"}
;               :on-click (fn []
;                           ;(reset! form-data (empty {}))
;                           (reset! drawer-state false)
;                           (reset! page-state :host))}
;              "Hosts")
;(ui/menu-item {:style    {:color "white"}
;               :on-click (fn []
;                           ;(reset! form-data (empty {}))
;                           (reset! drawer-state false)
;                           (reset! page-state :tasks))}
;              "Tasks")
;(ui/menu-item {:style    {:color "white"}
;               :on-click (fn []
;                           ;(reset! form-data (empty {}))
;                           (reset! drawer-state false)
;                           (reset! page-state :workflows))}
;              "Workflows")
;(ui/menu-item {:style    {:color "white"}
;               :on-click (fn []
;                           ;(reset! form-data (empty {}))
;                           (reset! drawer-state false)
;                           (reset! page-state :event))}
;              "Image Management")
;(ui/menu-item {:style    {:color "white"}
;               :on-click (fn []
;                           ;(reset! form-data (empty {}))
;                           (reset! drawer-state false)
;                           (reset! page-state :event))}
;              "Events"))))


(defn add-network
  [sel]
  {:did-mount
   (fn [state]
     (let [[content-ref] (:rum/args state)
           _ (info "node-state-init")
           uri-state (rum/cursor app-state :url)
           data {:nodes @node-state
                 :edges @node-state}
           options {:autoResize true
                    ;:layout {
                    ;         :randomSeed 41154115}
                    :height     "600px"
                    :nodes      {:shape "box"
                                 :fixed false
                                 :font  {
                                         :color "white"}}
                    :physics
                                {:solver "repulsion"}
                    :interaction
                                {:hover             true
                                 :dragNodes         true
                                 :navigationButtons false
                                 ;:selectConnectedEdges false
                                 :tooltipDelay      1}}
           network (js/vis.Network. (js/document.getElementById sel) data (clj->js options))
           _ (swap! app-state assoc :network network)
           _ (.on (js/vis.DataSet. @node-state) "*" (fn [e o]
                                                      (info "Network Update" e o)))
           ;(update-network o (clj->js options))))
           _ (.on network "click" (fn [e o]
                                    (let [node (first (.-nodes e))
                                          uri @uri-state
                                          node-dataset (js/vis.DataSet. (clj->js (:nodes @node-state)))
                                          node (.get node-dataset node)
                                          node-link (.-link node)]
                                      (reset! uri-state (str (url uri node-link)))
                                      (get-resource)
                                      (info "Clicked Node " node-link))))]
       (add-watch node-state ::node-state (fn [_ _ _ new-val]
                                            (do
                                              (info "Found new-val " new-val)
                                              (update-network
                                                (clj->js new-val)
                                                (clj->js (conj
                                                           options
                                                           {:nodes {:color  "#007bc3"
                                                                    :shadow false
                                                                    :size   60}})))))))


     ;         :hierarchical {
     ;                        :direction "LR"
     ;                        :enabled true}}})))))))
     state)

   :should-update
   (fn [old new] false)

   :will-unmount
   (fn [state]
     (let [[content-ref] (:rum/args state)]
       (remove-watch content-ref key))
     state)})

(defc network < rum/reactive
                (add-network "network")
  [content-ref]
  (rum/react node-state)
  [:div
   [:div {:id "network"}]])


(rum/defc show-dialog < rum/reactive
  [child title dialog-state submit-fn]
  [:div
   (ui/dialog {:title            title
               :modal            true
               :actions          (submit-fn dialog-state)
               :open             (rum/react dialog-state)
               :on-request-close #(reset! dialog-state false)}
              (child))])


(rum/defc inspector < rum/reactive
  [app-state data & [path]]
  (let [inspect (rum/cursor app-state :inspect-state)]
    (rum/react data)
    (ui/dialog {:title                    "Inspector"
                :modal                    false
                ;:action                   (ui/flat-button {:label        "Close"
                ;                                           :on-touch-tap #(reset! page-state (empty {}))})
                :open                     (rum/react inspect)
                :on-request-close         #(reset! inspect false)
                :auto-scroll-body-content true}

               [:div.margin
                [:pre.inspector {:key "inspector-view"}
                 (with-out-str (pprint/pprint (get-in @data path)))]])))



;{:kvm? true, :osversion "4.3", :imagerepo "http://172.17.92.50:9090/common/rhel", :rootpassword "dsadasd", :hostname "sah", :domainname "dell.com", :dnsservers "8.8.8.8", :installdisk "/dev/sda"}

;
;(defn save [form-state]
;  (let [nodeid (get-in @app-state [:nodeid])
;        _ (print "DEBUG formstate" @form-state)]
;    (api/put-object (str/join "-" ["profile" nodeid (.getTime (js/Date.))]) @form-state)))

;(defn form-handler [form-state]
;  (let [{:keys [kickstart osversion distro hostname rootpassword1 subPool rootpassword2 domainname subUser subPw]} (get-in @form-state [:host-state :form-state])
;        nodeid (get-in @app-state [:nodeid])
;        bootserver "172.17.92.50:8060"
;        repourl (str/join "/" ["http:/" bootserver distro osversion])
;        user-state (get-in @form-state [:user-state :form-state])
;        interface-state (get-in @form-state [:interface-state])
;        dns-state (get-in @form-state [:dns-state :form-state])
;        interfaces (api/make-network-device interface-state)
;        ;_ (pprint/pprint interfaces)
;        users (mapv #(api/make-users %) user-state)
;        partition (api/make-partitions "/dev/sda" "auto" "ext4")
;        dns (mapv #(:dnsserver %) dns-state)
;        workflow (api/make-rhel-workflow kickstart osversion repourl rootpassword1 hostname domainname users dns interfaces true "/dev/sda" partition subUser subPw subPool)
;        _ (api/put-object (str/join "-" ["wf" nodeid (.getTime (js/Date.))]) workflow)
;        _ (api/put-object (str/join "-" ["profile" nodeid (.getTime (js/Date.))]) @form-state)]
;    ;(pprint/pprint workflow)))
;    (api/post-workflow nodeid workflow)))
;(swap! app-state assoc :page-state :event)

;{:action-state true,
; :row-selected 0,
; :nodeid "5a1cc5268e7dfb0100603537",
; :host-state {:form-state {:osversion "7",
;                           :iso 1,
;                           :hostname "sah",
;                           :rootpassword1 "password",
;                           :subPool "8a85f9815e0f0d29015e0f5302b03351",
;                           :rootpassword2 "password",
;                           :domainname "dell.com",
;                           :isoselect "rhel-7.4",
;                           :subUser "DellEMCServiceProviders",
;                           :subPw "DellEMCSP#123"}},
; :interface-state {:row-state {},
;                   :form-state ({:device "p2p2",
;                                 :ipaddress "2.2.2.2",
;                                 :netmask "255.255.255.0",
;                                 :gateway "2.2.2.254"
;                                 {:device "p2p1",
;                                  :ipaddress "1.1.1.1",
;                                  :netmask "255.255.255.0",
;                                  :gateway "1.1.1.254"}},)}
; :dns-state {:row-state {}, :form-state ({:dnsserver "8.8.8.8"})},
; :user-state {:row-state {}, :form-state ({:username "gary", :password1 "password", :password2 "password"})}}







;vlan_range     (range vlan_start (+ vlan_start vlan_count))
;ipsub_range    (range ipgw3oct (+ ipgw3oct vlan_count))
;leaf_switches  (:leaf_switches @data)
;vlan-block     (map (fn [x y]
;                      (into {}
;                            {:vlan x
;                             :description customer_name
;                             :type "customer"
;                             :ipgw (str new_prefix "." y "." 1 "/24")}))
;
;                    vlan_range ipsub_range)
;customers (into {} (assoc-in {} [:customers]
;                             [{:customer-name customer_name
;                               :vni vni
;                               :vlan-block vlan-block
;                               :rbridge-id rbridge-id}]))]))



(rum/defc table-text-field
  [a label type]
  (ui/text-field {:key                  label
                  :id                   label
                  :floating-label-text  label
                  :floating-label-fixed true
                  :floating-label-style {:font-size "20px"}
                  :type                 type
                  :underline-show       false
                  :full-width           true
                  :value                (or @a "")
                  :on-change            (fn [e]
                                          (reset! a (.. e -target -value)))}))


(rum/defcs phy-table < rum/reactive
                       (rum/local false ::isMaster?)
                       (rum/local [] ::interfaces)
  [state phy-state bond-state]
  (let [form-state (rum/cursor-in phy-state [:form-state])
        row-state (rum/cursor-in phy-state [:row-state])
        type-state (rum/cursor-in row-state [:type])
        bonds-state (:form-state @bond-state)
        bonds (mapv :device bonds-state)
        isMaster? (::isMaster? state)
        interface-pick (::interfaces state)

        device (rum/cursor-in row-state [:device])
        ipaddress (rum/cursor-in row-state [:ipaddress])
        netmask (rum/cursor-in row-state [:netmask])
        gateway (rum/cursor-in row-state [:gateway])

        bondinterface (rum/cursor-in row-state [:bondinterface])]
    [:div
     (ui/table {:style                {:margin-top "15px"}
                :selectable           true
                ;:height               "200px"
                :fixed-header         true
                :display-row-checkbox false
                :on-cell-click        (fn [row col]
                                        (if (= 6 col)
                                          (remove-indexes-atom [row] form-state)))}
               (ui/table-header {:display-select-all  false
                                 :adjust-for-checkbox false}
                                (ui/table-row
                                  (ui/table-header-column {:key device} (table-text-field device "Device" "text"))
                                  (ui/table-header-column {:key ipaddress} (table-text-field ipaddress "IP Address" "text"))
                                  (ui/table-header-column {:key netmask} (table-text-field netmask "Netmask" "text"))
                                  (ui/table-header-column {:key gateway} (table-text-field gateway "Gateway" "text"))
                                  (when (> (count bonds) 0)
                                    (do
                                      [
                                       (ui/table-header-column (ui/select-field {:value                @bondinterface
                                                                                 :floating-label-text  "Bond"
                                                                                 :floating-label-style {:padding-bottom "20px"
                                                                                                        :font-size      "20px"}
                                                                                 :on-change            #(reset! bondinterface %3)
                                                                                 :auto-width           true
                                                                                 :full-width           true}
                                                                                (map #(ui/menu-item {:key % :value % :primary-text %})
                                                                                     bonds)))]))
                                  ;(ui/table-header-column (ui/checkbox {:label    "Master"
                                  ;                                      :checked  (rum/react isMaster?)
                                  ;                                      :on-check #(reset! isMaster? (not @isMaster?))}))]))



                                  (ui/table-header-column (ui/flat-button {:icon (icons/content-add-box)
                                                                           :on-click
                                                                                 (fn [e]
                                                                                   (swap! row-state assoc :isMaster? @(state ::isMaster?))
                                                                                   (swap! form-state conj @row-state)
                                                                                   (reset! row-state (empty {})))}))))

               (ui/table-body {:display-row-checkbox false}
                              (map (fn [row]
                                     (let [{:keys [device ipaddress netmask gateway]} row]
                                       (ui/table-row
                                         (ui/table-row-column (get row :device "N/A"))
                                         (ui/table-row-column (get row :ipaddress "N/A"))
                                         (ui/table-row-column (get row :netmask "N/A"))
                                         (ui/table-row-column (get row :gateway "N/A"))
                                         (ui/table-row-column (get row :bondinterface "N/A"))
                                         (ui/table-row-column (str (get row :isMaster? "N/A")))

                                         (ui/table-row-column (ui/flat-button {:icon
                                                                               (icons/toggle-indeterminate-check-box)})))))
                                   (rum/react form-state))))]))
;[:div
; (with-out-str (pprint/pprint @form-data))]]))



(defc interface-table [form-data]
  (let [bond-state (get-in @form-data [:interface-state :bond-state :form-state])
        phy-state (get-in @form-data [:interface-state :phy-state :form-state])
        vlan-state (get-in @form-data [:interface-state :vlan-state :form-state])
        i-state (merge bond-state phy-state vlan-state)
        _ (print i-state)]

    (ui/table-body {:display-row-checkbox false}
                   (map (fn [row]
                          [:div
                           [:p row]])
                        ;(let [k (keys row)]
                        ;  (ui/table-row
                        ;      (ui/table-row-column k)
                        ;      (ui/table-row-column (ui/flat-button {:icon
                        ;                                            (icons/toggle-indeterminate-check-box)})))))
                        i-state))))


(defc bond-table < rum/reactive
  [bond-state]
  (let [form-state (rum/cursor-in bond-state [:form-state])
        row-state (rum/cursor-in bond-state [:row-state])
        type-state (rum/cursor-in row-state [:type])
        device (rum/cursor-in row-state [:device])
        ipaddress (rum/cursor-in row-state [:ipaddress])
        netmask (rum/cursor-in row-state [:netmask])
        gateway (rum/cursor-in row-state [:gateway])
        bondparams (rum/cursor-in row-state [:bondparams])]

    [:div
     (ui/table {:style                {:margin-top "15px"}
                :selectable           true
                :fixed-header         true
                :display-row-checkbox false
                :on-cell-click        (fn [row col]
                                        (if (= 4 col)
                                          (remove-indexes-atom [row] form-state)))}
               (ui/table-header {:display-select-all  false
                                 :adjust-for-checkbox false}
                                (ui/table-row
                                  (ui/table-header-column (table-text-field device "Device" "text"))
                                  (ui/table-header-column (table-text-field ipaddress "IP Address" "text"))
                                  (ui/table-header-column (table-text-field netmask "NetMask" "text"))
                                  (ui/table-header-column (table-text-field gateway "Gateway" "text"))
                                  (ui/table-header-column (ui/flat-button {:icon (icons/content-add-box)
                                                                           :on-click
                                                                                 (fn [_]
                                                                                   (swap! row-state assoc :type "Bond")
                                                                                   (swap! row-state assoc :bondparams "802.3ad miimon=100")
                                                                                   (swap! form-state conj @row-state)
                                                                                   (reset! row-state (empty {}))
                                                                                   (swap! row-state assoc :type @type-state))}))))

               (ui/table-body {:display-row-checkbox false}
                              (map (fn [row]
                                     (let [{:keys [device ipaddress netmask gateway]} row]
                                       (ui/table-row
                                         (ui/table-row-column device)
                                         (ui/table-row-column ipaddress)
                                         (ui/table-row-column netmask)
                                         (ui/table-row-column gateway)
                                         (ui/table-row-column (ui/flat-button {:icon
                                                                               (icons/toggle-indeterminate-check-box)})))))
                                   (rum/react form-state))))
     [:div.row
      [:div.col.l2
       (table-text-field bondparams "Bond Params" "text")]]]))


;(defc vlan-table < rum/reactive
;  [vlan-state]
;  (let [form-state (rum/cursor-in vlan-state [:form-state])
;        row-state (rum/cursor-in vlan-state [:row-state])
;        interface (rum/cursor-in row-state [:interface])
;        vlan (rum/cursor-in row-state [:vlan])]
;
;    [:div
;     (ui/table {:style                {:margin-top "15px"}
;                :selectable           true
;                :fixed-header         true
;                :display-row-checkbox false
;                :on-cell-click        (fn [row col]
;                                        (if (= 4 col)
;                                          (remove-indexes-atom [row] form-state)))}
;               (ui/table-header {:display-select-all  false
;                                 :adjust-for-checkbox false}
;                                (ui/table-row
;                                  (ui/table-header-column (table-text-field interface "Interface" "text"))
;                                  (ui/table-header-column (table-text-field vlan "VLAN" "text"))
;                                  (ui/table-header-column (ui/flat-button {:icon (icons/content-add-box)
;                                                                           :on-click
;                                                                                 (fn [_]
;                                                                                   (swap! form-state conj @row-state)
;                                                                                   (reset! row-state (empty {})))}))))
;
;               (interface-table form-data))
;
;     [:div
;      (with-out-str (pprint/pprint @form-data))]]))

;(rum/defc interface-template < rum/reactive
;  [interface-data]
;  (let [phy-state (rum/cursor-in interface-data [:phy-state])
;        bond-state (rum/cursor-in interface-data [:bond-state])
;        vlan-state (rum/cursor-in interface-data [:vlan-state])
;        form-state (rum/cursor-in interface-data [:form-state])
;        row-state (rum/cursor-in interface-data [:row-state])
;
;        type-state (rum/cursor-in row-state [:type])]
;    [:div.row.box
;     [:div.col.l2
;      [:div.section "Interface Type"
;       (ui/select-field {:value      (str (rum/react type-state))
;                         :on-change  (fn [_ a b]
;                                       (swap! row-state assoc :type b))
;                         :auto-width true
;                         :full-width true}
;                        (map #(ui/menu-item {:key % :value % :primary-text %})
;                             ["Bond" "Bridge" "VLAN" "Physical"]))]]
;     [:div.col.l10
;      (condp = (rum/react type-state)
;        "Bond"
;        (bond-table bond-state)
;        "Bridge" [:p "Bridge"]
;        "VLAN" (vlan-table vlan-state)
;        "Physical" (phy-table phy-state bond-state)
;        (phy-table phy-state bond-state))]]))



;(rum/defc dns-template
;  [dns-data]
;  (let [form-state (rum/cursor-in dns-data [:form-state])
;        row-state (rum/cursor-in dns-data [:row-state])
;        dnsserver (rum/cursor row-state :dnsserver)]
;    [:div.box
;     (ui/table
;       {:selectable           true
;        :fixed-header         true
;        :fixed-footer         true
;        :display-row-checkbox false
;        :on-cell-click        (fn [row col]
;                                (if (= 1 col)
;                                  (remove-indexes-atom [row] form-state)))}
;       (ui/table-header {:display-select-all  false
;                         :adjust-for-checkbox false}
;                        (ui/table-row
;                          (ui/table-header-column
;                            (table-text-field dnsserver "DNS Server" "text"))
;                          (ui/table-header-column
;                            (ui/flat-button {:icon (icons/content-add-box)
;                                             :on-click
;                                                   (fn [r]
;                                                     (swap! form-state conj @row-state)
;                                                     (reset! row-state (empty {})))}))))
;       (ui/table-body {:display-row-checkbox false}
;                      (map (fn [row]
;                             (let [{:keys [dnsserver]} row]
;                               (ui/table-row
;                                 (ui/table-row-column dnsserver)
;                                 (ui/table-row-column (ui/flat-button {:icon (icons/toggle-indeterminate-check-box)})))))
;
;                           @form-state)))]))

;
;
;(rum/defc user-template < rum/reactive
;  [user-data]
;  (let [form-state (rum/cursor-in user-data [:form-state])
;        row-state (rum/cursor-in user-data [:row-state])
;        username (rum/cursor row-state :username)
;        password1 (rum/cursor row-state :password1)
;        password2 (rum/cursor row-state :password2)]
;    [:div.box
;     (ui/table {:selectable           true
;                :fixed-header         true
;                :display-row-checkbox false
;                :on-cell-click        (fn [row col]
;                                        (if (= 3 col)
;                                          (remove-indexes-atom [row] form-state)))}
;               (ui/table-header {:display-select-all  false
;                                 :adjust-for-checkbox false}
;                                (ui/table-row
;                                  (ui/table-header-column (table-text-field username "Username" "text"))
;                                  (ui/table-header-column (table-text-field password1 "Password" "password"))
;                                  (ui/table-header-column (table-text-field password2 "Password1" "password"))
;                                  (ui/table-header-column (ui/flat-button {:icon (icons/content-add-box)
;                                                                           :on-click
;                                                                                 (fn [r]
;                                                                                   (swap! form-state conj @row-state)
;                                                                                   (reset! row-state (empty {})))}))))
;
;               (ui/table-body {:display-row-checkbox false}
;                              (map (fn [row]
;                                     (let [{:keys [username password1 password2]} row
;                                           _ (when (not (= password1 password2)) (js/alert "passwords don't match "))]
;                                       (ui/table-row
;                                         (ui/table-row-column username)
;                                         (ui/table-row-column "REDACTED")
;                                         (ui/table-row-column "REDACTED")
;                                         (ui/table-row-column (ui/flat-button {:icon (icons/toggle-indeterminate-check-box)})))))
;
;                                   (rum/react form-state))))]))


;
;
;(rum/defcs provision-template < rum/reactive
;                                (api/ajax-mixin
;                                  (str/join "/" [api/rhdapi "templates" "metadata"])
;                                  :get
;                                  {}
;                                  ::kickstart)
;  [state host-data app-state]
;  (let [form-state (rum/cursor-in host-data [:form-state])
;        distroselected (rum/cursor-in form-state [:distro])
;        vesionselected (rum/cursor-in form-state [:osversion])
;        iso @(rum/cursor-in app-state [:iso])
;        versions (map :version (filter #(= (:name %) @distroselected) iso))
;        distros (set (map :name iso))
;        hostname (rum/cursor form-state :hostname)
;        domainname (rum/cursor form-state :domainname)
;        rootpassword1 (rum/cursor form-state :rootpassword1)
;        rootpassword2 (rum/cursor form-state :rootpassword2)
;        ks (rum/cursor form-state :kickstart)
;        subUser (rum/cursor form-state :subUser)
;        subPw (rum/cursor form-state :subPw)
;        subPool (rum/cursor form-state :subPool)
;        kickstart (if (> 0 (:status @(::kickstart state)))
;                    (let [templates @(::kickstart state)]
;                      (filterv #(re-matches #"\w+-ks" %)
;                               (map :name templates)))
;                    ["..."])]
;
;    ;(rum/react form-state)
;    ;isovec (map #(str/join "-" %) (mapv #(concat %) (map #(vals %) (map #(select-keys % [:name :version]) @iso))))]
;    [:div.box {:id "prov"}
;     (ui/select-field {:floating-label-text  "Distro"
;                       :floating-label-fixed true
;                       :floating-label-style {:font-size "16px"
;                                              :color     "#9e9e9e"}
;                       :value                (str (rum/react distroselected))
;                       :on-change            (fn [_ a b]
;                                               (swap! form-state assoc :distro b))
;                       ;(swap! form-state assoc :iso a))
;                       :auto-width           true
;                       :full-width           true
;                       :max-height           200}
;                      (map #(ui/menu-item {:key % :value % :primary-text %}) distros))
;
;
;     (ui/select-field {:floating-label-text  "Version"
;                       :floating-label-fixed true
;                       :floating-label-style {:font-size "16px"
;                                              :color     "#9e9e9e"}
;                       :value                (str (rum/react vesionselected))
;                       :on-change            (fn [_ a b]
;                                               (swap! form-state assoc :osversion b))
;                       :auto-width           true
;                       :full-width           true
;                       :max-height           200}
;                      (map #(ui/menu-item {:key % :value % :primary-text %})
;                           versions))
;
;     (ui/select-field {:floating-label-text  "Kickstart"
;                       :floating-label-fixed true
;                       :floating-label-style {:font-size "16px"
;                                              :color     "#9e9e9e"}
;                       :value                (rum/react ks)
;                       :on-change            (fn [_ a b]
;                                               (swap! form-state assoc :kickstart b))
;                       :auto-width           true
;                       :full-width           true
;                       :max-height           200}
;                      (map #(ui/menu-item {:key % :value % :primary-text %})
;                           kickstart))
;     ;
;     (table-text-field hostname "Hostname" "text")
;     (table-text-field domainname "Domain Name" "text")
;     (table-text-field rootpassword1 "Root Password1" "password")
;     (table-text-field rootpassword2 "Root Password2" "password")
;     (when (= "rhel" (rum/react distroselected))
;       [:div
;        (table-text-field subUser "RedHat User" "text")
;        (table-text-field subPw "RedHat Password" "text")
;        (table-text-field subPool "RedHat Pool" "text")])]))
;;[:div
;;   (with-out-str (pprint/pprint @(::kickstart state)))]))
;
;(defn force-rerender!
;  [app-state]
;  (let [comp (:rum/react-component app-state)
;        _ (print comp)]
;    (rum/request-render comp)))
;
;
;(rum/defc project-info < rum/reactive
;  [form-state]
;  (let [note-state (rum/cursor-in form-state [:note-state])]
;    [:div.row
;     [:div.col.l12
;      [:div.section "Notes"]
;
;      [:div.box
;       (ui/text-field {:style      {:background-color "white"
;                                    :width            "100%"}
;                       :multi-line true
;                       :rows       10
;                       :value      (or (rum/react note-state) "")
;                       :on-change  (fn [e]
;                                     (reset! note-state (.. e -target -value)))})]]]))
;
;
;(rum/defcs node-automation < rum/reactive
;                             (api/ajax-mixin
;                               (str/join "/" [api/rhdapi "nodes" (get @app-state :nodeid) "workflows"])
;                               :get
;                               {}
;                               ::workflows)
;                             (api/ajax-mixin
;                               (str/join "/" [api/rhdapi "nodes" (get @app-state :nodeid) "tags"])
;                               :get
;                               {}
;                               ::tags)
;                             (api/ajax-mixin
;                               (str/join "/" [api/rhdapi "nodes" (get @app-state :nodeid) "relations"])
;                               :get
;                               {}
;                               ::relations)
;  [state nodeid]
;  (let [workflows @(::workflows state)]
;    (ui/tabs
;      (ui/tab {:label "Workflows"}
;              [:div
;               (if-let [workflows @(::workflows state)]
;                 (with-out-str (pprint/pprint workflows))
;                 [:div "Loading..."])])
;      (ui/tab {:label "Tags"}
;              [:div
;               (if-let [tags @(::tags state)]
;                 (with-out-str (pprint/pprint tags))
;                 [:div "Loading..."])])
;      (ui/tab {:label "Relations"}
;              [:div
;               (if-let [relations @(::relations state)]
;                 (with-out-str (pprint/pprint relations))
;                 [:div "Loading..."])]))))
;
;
;(defc lspci-template [lspci]
;  [:div.container
;   (ui/table {:id                   "pci-table"
;              :style                {:margin-top "15px"}
;              :selectable           false
;              :fixed-header         true
;              :fixed-footer         true
;              :display-row-checkbox false
;              :multi-selectable     false
;              :height               "600px"}
;             (ui/table-header {:enable-select-all   false
;                               :adjust-for-checkbox false
;                               :display-select-all  false}
;                              (ui/table-header-column "Slot")
;                              (ui/table-header-column "Class")
;                              (ui/table-header-column "Vendor")
;                              (ui/table-header-column "Device"))
;             (ui/table-body {:show-row-hover       true
;                             :striped-rows         true
;                             :adjust-for-checkbox  false
;                             :pre-scan-rows        false
;                             :display-row-checkbox false}
;                            (map (fn [{:keys [Slot Class Vendor Device]} %]
;                                   (ui/table-row {:selectable false}
;                                                 (ui/table-row-column Slot)
;                                                 (ui/table-row-column Class)
;                                                 (ui/table-row-column Vendor)
;                                                 (ui/table-row-column Device)))
;                                 lspci)))])



;[{:scsiId "10:2:0:0",
;  :virtualDisk "/c0/v0",
;  :esxiWwid "naa.61866da0bbc483002144053a0a3ba95a",
;  :devName "sda",
;  :identifier 0,
;  :linuxWwid
;  "/dev/disk/by-id/scsi-361866da0bbc483002144053a0a3ba95a"}]

;
;(defc drive-template [lsscsi driveid]
;  [:div.container
;   (ui/table {:id                   "drive-table"
;              :style                {:margin-top "15px"}
;              :selectable           false
;              :fixed-header         true
;              :fixed-footer         true
;              :display-row-checkbox false
;              :multi-selectable     false
;              :height               "600px"}
;             (ui/table-header {:enable-select-all   false
;                               :adjust-for-checkbox false
;                               :display-select-all  false}
;                              (ui/table-header-column "Info")
;                              (ui/table-header-column "Type")
;                              (ui/table-header-column "Vendor")
;                              (ui/table-header-column "Model")
;                              (ui/table-header-column "Revision")
;                              (ui/table-header-column "Path")
;                              (ui/table-header-column "Size"))
;             (ui/table-body {:show-row-hover       true
;                             :striped-rows         true
;                             :adjust-for-checkbox  false
;                             :pre-scan-rows        false
;                             :display-row-checkbox false}
;                            (map (fn [{:keys [scsiInfo peripheralType vendorName modelName revisionString devicePath size]} %]
;                                   (ui/table-row {:selectable false}
;                                                 (ui/table-row-column (str/replace scsiInfo #"[\[\]]" ""))
;                                                 (ui/table-row-column peripheralType)
;                                                 (ui/table-row-column vendorName)
;                                                 (ui/table-row-column modelName)
;                                                 (ui/table-row-column revisionString)
;                                                 (ui/table-row-column devicePath)
;                                                 (ui/table-row-column size)))
;                                 lsscsi)))])
;
;;#TODO fix sorting
;(defc ipmi-events-template [events]
;  [:div.container
;   (ui/table {:id                   "ipmi-events-table"
;              :style                {:margin-top "15px"}
;              :selectable           false
;              :fixed-header         true
;              :fixed-footer         true
;              :display-row-checkbox false
;              :multi-selectable     false
;              :height               "600px"}
;             (ui/table-header {:enable-select-all   false
;                               :adjust-for-checkbox false
;                               :display-select-all  false}
;                              (ui/table-header-column "Date")
;                              (ui/table-header-column "Time")
;                              (ui/table-header-column "Topic")
;                              (ui/table-header-column "Description")
;                              (ui/table-header-column "Status"))
;             (ui/table-body {:show-row-hover       true
;                             :striped-rows         true
;                             :adjust-for-checkbox  false
;                             :pre-scan-rows        false
;                             :display-row-checkbox false}
;                            (map (fn [e]
;                                   (let [[date time topic description status] (fnext e)]
;                                     (ui/table-row {:selectable false}
;                                                   (ui/table-row-column date)
;                                                   (ui/table-row-column time)
;                                                   (ui/table-row-column topic)
;                                                   (ui/table-row-column description)
;                                                   (ui/table-row-column status))))
;                                 (sort events))))])
;
;
;(rum/defcs provision-master < rum/reactive
;  [state form-data app-state]
;  (let [interface-state (rum/cursor-in form-data [:interface-state])
;        dns-state (rum/cursor-in form-data [:dns-state])
;        user-state (rum/cursor-in form-data [:user-state])
;        host-state (rum/cursor-in form-data [:host-state])
;        note-state (rum/cursor-in form-data [:note-state])
;        nodeid (rum/cursor-in app-state [:nodeid])
;        profile-keys (filterv #(re-matches #"profile.*" %) (api/get-keys))
;        nodepattern (re-pattern (str "profile-" @nodeid "-.*"))
;        profile-select (rum/cursor-in form-data [:profile-select])
;        save-profile (rum/cursor app-state :save-profile)]
;    (rum/react app-state)
;    ;(rum/react form-data)
;
;    [:div {:id "host-child"}
;     [:div.row
;      [:div.col.l4
;       [:div.section [:span [:h5 "Host Configuration"] [:h6 "NodeId: " @nodeid]]]
;       [:div.divider]]
;      [:div.col.l3]
;      [:div.col.l5
;       [:div.section [:h5 "Load Profile"]]
;       (ui/select-field {
;                         :floating-label-fixed true
;                         :floating-label-style {:font-size "22px"
;                                                :color     "#999999"}
;                         :value                (str (rum/react profile-select))
;                         ;:value                (str profile-select)
;                         :on-change            (fn [_ a b]
;                                                 (reset! form-data (api/get-object b)))
;                         ;(swap! form-state assoc :iso a))
;                         :auto-width           true
;                         :full-width           true
;                         :max-height           200}
;                        (map #(ui/menu-item {:key % :value % :primary-text %})
;                             (filterv #(re-matches nodepattern %) profile-keys)))]
;      (ui/tabs
;        (ui/tab {:label "General"}
;                [:div {:style {:padding-top "20px"}}
;                 [:div.row {:id "kt1"}
;                  [:div.col.l2
;                   (provision-template host-state app-state)]
;                  [:div.col.l4 {:id "dns"}
;                   [:div.section "DNS Configuration"]
;                   (dns-template dns-state)]
;                  [:div.col.s6
;                   [:div.section "User Configuration"]
;                   (user-template user-state)]
;                  [:div.col.l10 {:id "intc"}
;                   [:div.section "Interface Configuration"]
;                   (interface-template interface-state)]]])
;        (ui/tab {:label "BIOS"}
;                [:div])
;        (ui/tab {:label "Disk"}
;                [:div])
;        (ui/tab {:label "Advanced Networking"}
;                [:div])
;        (ui/tab {:label "catalog"}
;                [:div.box
;                 (ui/tabs
;                   (ui/tab {:label "Drives"}
;                           (if-let [lsscsi (:data (get-in @app-state [:catalogs :lsscsi]))]
;                             (if-let [driveid (:data (get-in @app-state [:catalogs :driveId]))]
;                               (drive-template lsscsi driveid)
;                               (ui/linear-progress {:mode "indeterminate"}))))
;                   (ui/tab {:label "PCI"}
;                           (if-let [lspci (:data (get-in @app-state [:catalogs :lspci]))]
;                             (lspci-template lspci)
;                             (ui/linear-progress {:mode "indeterminate"})))
;                   (ui/tab {:label "IPMI Logs"}
;                           (if-let [ipmi-sel (:data (get-in @app-state [:catalogs :ipmi-sel]))]
;                             (ipmi-events-template ipmi-sel)
;                             (ui/linear-progress {:mode "indeterminate"}))))])
;        (ui/tab {:label "automation"}
;                (node-automation nodeid))
;        (ui/tab {:label "Project Notes"}
;                [:div
;                 (project-info form-data)]))]
;     ;
;     [:div.row
;      [:div.center
;       [:div
;        (ui/raised-button {:label    "Save"
;                           :style    {:margin "12px"}
;                           :on-click #(save form-data)})
;        (ui/raised-button {:label    "Submit"
;                           :style    {:margin "12px"}
;                           :primary  true
;                           :on-click #(form-handler form-data)})]]]]))

;[:div (with-out-str (pprint/pprint @form-data))])
;;[:div (with-out-str (pprint/pprint @app-state))])


(rum/defc host-table-actions [nodeid form-data]
  (let [action-state (rum/cursor-in form-data [:action-state])]
    (ui/raised-button {:on-click #(reset! action-state true)
                       :label    "Provision"})))

(defn is-selected? [form-data row]
  (let [row-selected (rum/cursor-in form-data [:row-selected])]
    (boolean (= @row-selected row))))

(defc status [s]
  [:svg {:height "100%" :width "100%"}
   [:circle {:cx "10" :cy "10" :r "7" :stroke "black" :fill "green"}]])

(defc jet-icon []
  (ui/svg-icon
    {:hover-color "yellow"
     :color       "white"
     :viewBox     "0 0 511.994 511.994"}
    ;:style   {:enable-background:new "0 0 511.994 511.994"}}
    [:path {:d "M510.868,362.698l-34.133-59.733c-1.519-2.662-4.343-4.301-7.407-4.301h-5.751l13.961-48.853    c1.126-3.951-0.717-8.141-4.386-9.975l-7.629-3.814l27.537-34.423c1.749-2.176,2.313-5.069,1.519-7.748    c-0.794-2.68-2.842-4.796-5.487-5.683l-20.745-6.912l8.994-24.738c1.476-4.079-0.333-8.61-4.207-10.547l-17.067-8.533c-3.123-1.562-6.878-1.067-9.489,1.254l-58.308,51.831l12.638-69.53c0.452-2.492-0.222-5.052-1.843-6.989c-1.621-1.937-4.011-3.072-6.537-3.072h-25.6c-2.039,0-4.011,0.725-5.555,2.057L243.604,213.936l-37.7,7.535    c-3.447-2.56-8.38-4.872-15.625-6.502c-10.812-2.441-24.755-2.978-39.253-1.51c-14.498,1.459-28.049,4.762-38.161,9.301    c-13.099,5.88-17.502,12.536-18.492,18.039l-88.09,24.03c-3.994,1.092-6.63,4.89-6.246,9.011c0.375,4.122,3.661,7.381,7.791,7.723    l101.623,8.431l142.805,25.199l99.046,83.328c0.819,0.7,1.775,1.229,2.79,1.57l1.604,0.538c0.87,0.29,1.783,0.435,2.697,0.435    c0.7,0,1.391-0.085,2.074-0.256l34.133-8.533c4.574-1.143,7.347-5.777,6.212-10.351l-14.976-59.904l12.279,1.886l83.9,50.338    c1.289,0.777,2.756,1.195,4.258,1.22l17.067,0.265c0.034,0,0.077,0,0.119,0c2.244,0,4.403-1.007,6.007-2.586    c1.604-1.604,2.526-3.925,2.526-6.212C511.994,365.446,511.602,363.987,510.868,362.698z M472.084,200.488l-19.669,24.585    l10.104-27.776L472.084,200.488z M370.085,127.997h12.22l-14.165,77.901l-44.587,7.432h-53.026L370.085,127.997z M152.741,230.44    c23.441-2.389,38.229,1.553,42.539,4.292c-3.669,3.541-17.365,10.317-40.832,12.681c-23.467,2.355-38.238-1.553-42.539-4.292    C115.578,239.588,129.274,232.804,152.741,230.44z M359.802,383.374l-98.321-82.714c-1.143-0.964-2.526-1.613-4.011-1.877    l-145.843-25.702l-54.221-4.514l44.075-12.023c3.439,2.492,8.328,4.736,15.411,6.332c6.827,1.536,14.899,2.313,23.569,2.313    c5.069,0,10.334-0.265,15.684-0.802c14.498-1.459,28.049-4.762,38.161-9.301c12.732-5.717,17.271-12.177,18.424-17.587l35.575-7.1    h75.955c0.469,0,0.939-0.034,1.408-0.119l51.174-8.533c0.102-0.017,0.196-0.034,0.299-0.051h0.009    c1.579-0.316,2.97-1.058,4.079-2.074l72.38-64.341l5.103,2.551l-31.539,86.716c-0.247,0.674-0.239,1.357-0.307,2.039    l-37.692,26.923L288.72,290.25c-4.13,0.691-7.151,4.275-7.125,8.474c0.026,4.198,3.089,7.74,7.236,8.38l78.703,12.109    l14.643,58.573L359.802,383.374z M405.447,308.417c-0.947-0.572-1.997-0.947-3.098-1.118l-58.291-8.977l49.877-8.311    c1.28-0.213,2.492-0.717,3.558-1.468l55.552-39.68l6.135,3.072l-15.121,52.924c-0.734,2.577-0.222,5.342,1.391,7.484    c1.613,2.142,4.13,3.388,6.81,3.388h12.117l24.38,42.667L405.447,308.417z"}]))

(defc jet []
  [:svg {:height  "50"
         :width   "50"
         :id      "Layer_1"
         :x       "0px"
         :y       "0px"
         :viewBox "0 0 511.994 511.994"
         :style   {:enable-background:new "0 0 511.994 511.994"}}
   ;[:xml {:space "preserve" :width "512px" :height "512px"}]}
   [:g
    [:path {:d    "M510.868,362.698l-34.133-59.733c-1.519-2.662-4.343-4.301-7.407-4.301h-5.751l13.961-48.853    c1.126-3.951-0.717-8.141-4.386-9.975l-7.629-3.814l27.537-34.423c1.749-2.176,2.313-5.069,1.519-7.748    c-0.794-2.68-2.842-4.796-5.487-5.683l-20.745-6.912l8.994-24.738c1.476-4.079-0.333-8.61-4.207-10.547l-17.067-8.533c-3.123-1.562-6.878-1.067-9.489,1.254l-58.308,51.831l12.638-69.53c0.452-2.492-0.222-5.052-1.843-6.989c-1.621-1.937-4.011-3.072-6.537-3.072h-25.6c-2.039,0-4.011,0.725-5.555,2.057L243.604,213.936l-37.7,7.535    c-3.447-2.56-8.38-4.872-15.625-6.502c-10.812-2.441-24.755-2.978-39.253-1.51c-14.498,1.459-28.049,4.762-38.161,9.301    c-13.099,5.88-17.502,12.536-18.492,18.039l-88.09,24.03c-3.994,1.092-6.63,4.89-6.246,9.011c0.375,4.122,3.661,7.381,7.791,7.723    l101.623,8.431l142.805,25.199l99.046,83.328c0.819,0.7,1.775,1.229,2.79,1.57l1.604,0.538c0.87,0.29,1.783,0.435,2.697,0.435    c0.7,0,1.391-0.085,2.074-0.256l34.133-8.533c4.574-1.143,7.347-5.777,6.212-10.351l-14.976-59.904l12.279,1.886l83.9,50.338    c1.289,0.777,2.756,1.195,4.258,1.22l17.067,0.265c0.034,0,0.077,0,0.119,0c2.244,0,4.403-1.007,6.007-2.586    c1.604-1.604,2.526-3.925,2.526-6.212C511.994,365.446,511.602,363.987,510.868,362.698z M472.084,200.488l-19.669,24.585    l10.104-27.776L472.084,200.488z M370.085,127.997h12.22l-14.165,77.901l-44.587,7.432h-53.026L370.085,127.997z M152.741,230.44    c23.441-2.389,38.229,1.553,42.539,4.292c-3.669,3.541-17.365,10.317-40.832,12.681c-23.467,2.355-38.238-1.553-42.539-4.292    C115.578,239.588,129.274,232.804,152.741,230.44z M359.802,383.374l-98.321-82.714c-1.143-0.964-2.526-1.613-4.011-1.877    l-145.843-25.702l-54.221-4.514l44.075-12.023c3.439,2.492,8.328,4.736,15.411,6.332c6.827,1.536,14.899,2.313,23.569,2.313    c5.069,0,10.334-0.265,15.684-0.802c14.498-1.459,28.049-4.762,38.161-9.301c12.732-5.717,17.271-12.177,18.424-17.587l35.575-7.1    h75.955c0.469,0,0.939-0.034,1.408-0.119l51.174-8.533c0.102-0.017,0.196-0.034,0.299-0.051h0.009    c1.579-0.316,2.97-1.058,4.079-2.074l72.38-64.341l5.103,2.551l-31.539,86.716c-0.247,0.674-0.239,1.357-0.307,2.039    l-37.692,26.923L288.72,290.25c-4.13,0.691-7.151,4.275-7.125,8.474c0.026,4.198,3.089,7.74,7.236,8.38l78.703,12.109    l14.643,58.573L359.802,383.374z M405.447,308.417c-0.947-0.572-1.997-0.947-3.098-1.118l-58.291-8.977l49.877-8.311    c1.28-0.213,2.492-0.717,3.558-1.468l55.552-39.68l6.135,3.072l-15.121,52.924c-0.734,2.577-0.222,5.342,1.391,7.484    c1.613,2.142,4.13,3.388,6.81,3.388h12.117l24.38,42.667L405.447,308.417z"
            :fill "#FFFFFF"}]]])
;
;[:svg {:height "50" :width "200"}
; [:path {:d "M150 0 L75 200 L225 100 Z"
;         :fill "#FFFFFF"}]])


(rum/defc footer-template < rum/reactive [app-state]

  [:footer.page-footer
   [:div.row
    [:div.footer-copyright.col.l2 "© 2017 DELL-EMC"]
    [:div.col.l6.right {:style {:text-align "right"}}
     "version 0.0.1"]]])

(declare host-table)
(declare event-table)

;
;(defc input-component
;  []
;  [:div
;   [:p "katfish: http://takagi:8765"]
;   [:p "emulator http://localhost:5000"]
;   [:p "14G https://172.29.209.35"]
;
;   [:input {:type              "text"
;            :allow-full-screen true
;            :id                "comment"
;            ;:value             (get @app-state :url)
;            :class             ["input_active" "input_error"]
;            :style             {:margin-left 5}
;            :on-change         #(swap! app-state assoc :url (.. % -target -value))}]
;   ;[:button {:label "Run" :on-click #(rum/mount (home-page app-state) (js/document.getElementById "app"))}]
;   [:button {:label "Run" :on-click #(get-resource)}]])

(defc main-body < rum/reactive [app-state]
  (let [page-state (rum/cursor-in app-state [:page-state])
        select-state (rum/cursor app-state :selected)
        url (rum/cursor app-state :url)]
    [:div.container
     [:div {:id "selector"}
      [:div.row
       [:div.col.l4
        (ui/text-field {:id                  "urlselect"
                        :style               {:font-size        "14px"
                                              :background-color "white"
                                              :width            "60%"}

                        :underline-show      false
                        :value               (or (rum/react url) nil)
                        :floating-label-text "Redfish Enpoint"
                        :on-change           #(swap! app-state assoc :url (.. % -target -value))})
        (ui/raised-button {:label    "Go"
                           :primary  true
                           :on-click #(get-resource)})
        [:pre {:key "inspector-view"}
         (with-out-str (pprint/pprint (rum/react select-state)))]]
       [:div.col.l8
        (network node-state)]]]]))
      ;[:div.row
      ; [:div
      ;  [:pre {:key "inspector-view"}
      ;   (with-out-str (pprint/pprint (rum/react node-state)))]]]]]))
;(condp = (rum/react page-state)
;:host (host-table hosts form-data app-state)
;:event (event-table hosts form-data app-state)
;:splash (splash app-state)
;:tasks (task-table)
;:workflows (workflows-table)
;(task-table)]))

(defc home-page [app-state]
  (let [test-project (rum/cursor app-state ::test-project)
        dialog-state (rum/cursor app-state :dialog-state)
        inspect-state (rum/cursor app-state ::inspect-state)]
    ;(api/load-data app-state)
    (ui/mui-theme-provider
      {:mui-theme (get-mui-theme {:palette {:primary1-color "#027EC0"}})}
      [:div
       (app-bar app-state)
       (app-menu app-state)
       (inspector app-state app-state)
       (main-body app-state)
       ;[:div
       ; (with-out-str (pprint/pprint @app-state))]
       (footer-template app-state)])))


;
;(defn load-node-data [nodeid]
;  {:will-mount
;   (fn [state]
;     (let [*data (atom nil)
;           comp (:rum/react-component state)]
;       (GET "/hello" {:params {:foo "foo"}})))})
;;  (fn [data]
;;    (reset! *data data)
;;    (rum/request-render comp)))
;;(assoc state key *data)))})
;

;(rum/defcs user-info < (ajax-mixin "/api/user/info" ::user)
;  [state])
;_ (api/update-active-workflows app-state)

;(defn get-active-workflows
;  "Retrieve nodes. "
;  [app-state nodeid]
;  (let [mock @(rum/cursor app-state :mock)]
;    (print "DEBUG: Calling get-active-workflows for node id " nodeid)
;    (if (not mock)
;      (let [url (str/join "/" [rhdapi "nodes" nodeid "workflows?active=true"])
;            _ (print "send request to " url)
;            nodekey (keyword nodeid)
;            request {:with-credentials? false
;                     :timeout           5000}]
;        (go
;          (let [response (<! (http/get url request))]
;            (if-not (:success response)
;              (print "error in response")
;              (swap! app-state assoc [:wf-alert] (count (:body response)))))))
;      ;(fn []
;      ;  (let [wfcount (count (:body response))]
;      ;    (print "WFCOUNT " wfcount)
;      ;    ;(swap! app-state assoc-in [:node] { nodekey {:status (:body response)}})
;      ;    (swap! app-state assoc [:wf-alert] wfcount)))))))
;      ;      #_(send-snackbar-message "get-models" "Error getting  models" true))))))
;      ;TODO fix
;      (swap! app-state assoc-in [:nodes] mock/mock-workflows))))

;(def get-hosts
;  (let [coll (get-in @app-state [:nodes])
;        hosts (into [] (utils/get-compute coll))]
;    hosts))

;
;
;(rum/defcs host-table < rum/reactive
;                        (api/ajax-mixin
;                          (str/join "/" [api/rhdapi "workflows?active=true"])
;                          :get
;                          {}
;                          ::workflows)
;  [state hosts form-data app-state]
;  (let [
;        hostmap (keep-indexed vector (utils/get-compute @hosts))
;        dns-state (rum/cursor-in form-data [:dns-state])
;        user-state (rum/cursor-in form-data [:user-state])
;        host-state (rum/cursor-in form-data [:host-state])
;        interface-state (rum/cursor-in form-data [:interface-state])
;        action-state (rum/cursor-in app-state [:action-state])
;        row-selected (rum/cursor-in form-data [:row-selected])
;        row-select (rum/cursor-in app-state [:row-select])
;        node-selected (rum/cursor-in app-state [:nodeid])
;        catalogs (rum/cursor-in app-state [:catalogs])]
;    ;wf-alerts (rum/cursor-in app-state [:wf-alert])
;    ;_ (api/update-active-workflows app-state)]
;    (rum/react form-data)
;    (rum/react app-state)
;    [:div.container
;     (ui/table {:id                    "host-table"
;                :selectable            true
;                :deselect-on-clickaway true
;                :fixed-header          true
;                :selected              (rum/react row-selected)
;                :on-row-selection      (fn [row]
;                                         (let [rownum (first (js->clj row))]
;                                           (reset! form-data (empty {}))
;                                           (reset! row-selected rownum)
;                                           (reset! row-select (not @row-select))
;                                           (when (not (nil? @row-selected))
;                                             (do
;                                               (reset! node-selected (:id (fnext (nth hostmap rownum))))
;                                               (api/ajax-wrapper
;                                                 (str/join "/" [api/rhdapi "nodes" (:id (fnext (nth hostmap rownum))) "catalogs" "lspci"])
;                                                 :get
;                                                 {}
;                                                 (fn [[ok response]]
;                                                   (swap! catalogs assoc :lspci response)))
;                                               (api/ajax-wrapper
;                                                 (str/join "/" [api/rhdapi "nodes" (:id (fnext (nth hostmap rownum))) "catalogs" "ipmi-sel"])
;                                                 :get
;                                                 {}
;                                                 (fn [[ok response]]
;                                                   (swap! catalogs assoc :ipmi-sel response)))
;                                               (api/ajax-wrapper
;                                                 (str/join "/" [api/rhdapi "nodes" (:id (fnext (nth hostmap rownum))) "catalogs" "lsscsi"])
;                                                 :get
;                                                 {}
;                                                 (fn [[ok response]]
;                                                   (swap! catalogs assoc :lsscsi response)))
;                                               (api/ajax-wrapper
;                                                 (str/join "/" [api/rhdapi "nodes" (:id (fnext (nth hostmap rownum))) "catalogs" "driveId"])
;                                                 :get
;                                                 {}
;                                                 (fn [[ok response]]
;                                                   (swap! catalogs assoc :driveId response)))))))}
;
;
;               ;(api/get-interfaces app-state nodeid)))))}
;               ;(force-rerender! app-state)))}
;               (ui/table-header {:display-select-all  false
;                                 :adjust-for-checkbox true}
;                                (ui/table-row
;                                  (ui/table-header-column "Id")
;                                  (ui/table-header-column "Name")
;                                  (ui/table-header-column "Type")
;                                  (ui/table-header-column "Status")
;                                  (ui/table-header-column "Workflow Alerts")))
;               (ui/table-body {:show-row-hover        true
;                               :striped-rows          true
;                               :deselect-on-clickaway false}
;                              (map (fn [host]
;                                     (let [index (first host)
;                                           m (fnext host)
;                                           nodeid (:id m)
;                                           coll (assoc m :status (status "green"))
;                                           wfcount (if-let [workflows @(::workflows state)]
;                                                     (count (filter #(= (:node %) nodeid) workflows))
;                                                     0)
;                                           coll (assoc coll :wfalerts (workflow-alerts wfcount nodeid))]
;                                       (ui/table-row {:striped  true
;                                                      :selected (is-selected? form-data index)}
;                                                     (map #(ui/table-row-column %) coll))))
;                                   hostmap)))
;
;     [:div {:style {:margin-top "20px"}}
;      (when (not (nil? @row-selected))
;        [:div.box {:style {:margin-bottom "100px"}}
;         (provision-master form-data app-state)])]]))
;[:div
; [:div [:p "form-data"]
;  (with-out-str (pprint/pprint (rum/react form-data)))]
; [:div [:p "app-state"]
;  (with-out-str (pprint/pprint (rum/react app-state)))]]]]))
;
;(rum/defc dhcp-table
;  [app-state])

;
;(defc save-profile < rum/reactive
;  [app-state]
;  (let [save-profile (rum/cursor app-state :save-profile)
;        profile (atom "")
;        _ (print "foo")]
;    (ui/dialog {:title            "Save Profile"
;                :modal            false
;                ;:actions          #(print "action")
;                ;:open             (rum/react save-profile)
;                :open             true
;                :on-request-close #(reset! save-profile false)}
;               ;(table-text-field profile "Save Profile"))))
;               [:div
;                [:p "Foo"]])))



;
;(rum/defc event-table < rum/reactive
;  [hosts form-data app-state]
;  (let [_ (reset! form-data (empty {}))
;        wf-keys (filterv #(re-matches #"wf.*" %) (api/get-keys))
;        profile-select (rum/cursor-in form-data [:profile-select])]
;    (rum/react form-data)
;    [:div.container
;     [:div {:id "event-master"}
;      [:div.row
;       [:div.section.col.l12 [:h5 "Events"]]]
;
;      (ui/table {:id               "events-table"
;                 :selectable       true
;                 :fixed-header     true
;                 :on-row-selection #(print %)}
;                ;:on-row-selection #(print "row " %)}
;                (ui/table-header {:display-select-all  false
;                                  :adjust-for-checkbox true}
;                                 (ui/table-row
;                                   (ui/table-header-column "Time")
;                                   (ui/table-header-column "Node")
;                                   (ui/table-header-column "Workflow")
;                                   (ui/table-header-column "Details...")))
;
;                (ui/table-body {:show-row-hover        true
;                                :striped-rows          true
;                                :deselect-on-clickaway false}
;                               (map (fn [key]
;                                      (let [wf (api/get-object key)
;                                            [_ node time] (str/split key #"-")
;                                            wf-name (:name wf)
;                                            datetime (str (js/Date time))
;                                            _ (print "AA" (js/Date time))]
;                                        (ui/table-row
;                                          (ui/table-row-column datetime)
;                                          (ui/table-row-column node)
;                                          (ui/table-row-column wf-name)
;                                          (ui/table-row-column "linkme"))))
;                                    wf-keys)))]]))




