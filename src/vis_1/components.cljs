(ns vis-1.components
  (:require
    [vis-1.state :refer [app-state node-state add-graph]]
    [vis-1.utils :as utils :refer [update-network footer-template jet-icon]]
    [taoensso.timbre :as timbre :refer [info]]
    [cemerick.url :refer [url url-encode]]
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


(defc app-bar < rum/reactive
  [app-state]
  (let [dialog-state (rum/cursor app-state :dialog-state)
        inspect-state (rum/cursor app-state :inspect-state)
        selected (rum/cursor app-state :selected)
        url (rum/cursor app-state :url)]

    (ui/app-bar {:title "RedFish Inspector"}
                (ui/icon-button {:on-click (fn []
                                             (reset! node-state (add-graph))
                                             (reset! selected [])
                                             (reset! url []))}
                                (icons/action-info {:color "white"}))
                (ui/icon-button {:on-click #(reset! inspect-state (not @inspect-state))}
                                (jet-icon)))))

(defn add-network
  [sel]
  {:did-mount
   (fn [state]
     (let [[content-ref] (:rum/args state)
           _ (info "node-state-init")
           uri-state (rum/cursor app-state :url)
           node-state vis-1.state/node-state
           data {:nodes []
                 :edges []}
           options {:autoResize true
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
                                 :tooltipDelay      1}}
           network (js/vis.Network. (js/document.getElementById sel) data (clj->js options))
           _ (swap! app-state assoc :network network)
           _ (.on (js/vis.DataSet. @node-state) "*" (fn [e o]
                                                      (info "Network Update" e o)))
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
                                              (update-network
                                                new-val
                                                (conj options
                                                      {:nodes {:color  "#007bc3"
                                                               :shadow false
                                                               :size   60}}))))))

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


(rum/defc inspector < rum/reactive
  [app-state data & [path]]
  (let [inspect (rum/cursor app-state :inspect-state)]
    (rum/react data)
    (ui/dialog {:title                    "Inspector"
                :modal                    false
                :open                     (rum/react inspect)
                :on-request-close         #(reset! inspect false)
                :auto-scroll-body-content true}

               [:div.margin
                [:pre.inspector {:key "inspector-view"}
                 (with-out-str (pprint/pprint (get-in @data path)))]])))


(defc main-body < rum/reactive [app-state]
  (let [select-state (rum/cursor app-state :selected)
        url (rum/cursor app-state :url)]
    (rum/react node-state)
    [:div.main-body
     [:div {:id "selector"}]
     (inspector app-state node-state)
     [:div.row
      [:div.col.l4
       (ui/text-field {:id                  "urlselect"
                       :style               {:font-size        "14px"
                                             :background-color "white"
                                             :width            "60%"}

                       :underline-show      false
                       :value               (or (rum/react url) "")
                       :floating-label-text "Redfish Enpoint"
                       :on-change           #(swap! app-state assoc :url (.. % -target -value))})
       (ui/raised-button {:label    "Go"
                          :primary  true
                          :on-click #(get-resource)})
       [:pre {:key "inspector-view"}
        (with-out-str (pprint/pprint (rum/react select-state)))]]
      [:div.col.l8
       (network node-state)]]]))



(defc home-page [app-state]
  (let [test-project (rum/cursor app-state ::test-project)
        dialog-state (rum/cursor app-state :dialog-state)
        inspect-state (rum/cursor app-state ::inspect-state)]

    (ui/mui-theme-provider
      {:mui-theme (get-mui-theme {:palette {:primary1-color "#027EC0"}})}
      [:div.top
       (app-bar app-state)
       [:div.main-body
        (main-body app-state)]
       [:div.page-footer
        (footer-template app-state)]])))


