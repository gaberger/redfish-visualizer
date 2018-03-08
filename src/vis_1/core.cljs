(ns vis-1.core
  (:require
    ;[antizer.rum :as ant]
    ;[antizer.core :as ac]
    ;[clojure.walk :refer [keywordize-keys]]
    [rum.core :as rum :refer [defc]]
    [ajax.core :refer [GET POST ajax-request json-request-format json-response-format]]
    [cljsjs.vis]
    [cljs.pprint :refer [pprint]]
    [clojure.string :as str]
    [goog.crypt.base64 :as base64]
    [taoensso.timbre
     :refer-macros [info debug]]))

(enable-console-print!)

(info "test")

;TODO Use odata as root odata

(defn pp [t r]
  (print t)
  (pprint r))

(defonce app-state (atom {}))

;(def config {:host "takagi" :port 8765})
(def config {:host "localhost" :port 5000})

(def node-state (atom js/Object))

(defn json->clj [string]
  (try
    (js->clj (.parse js/JSON string) :keywordize-keys true)
    (catch js/SyntaxError err
      (print "Error: Error parsing object " err))))

;
(def nodes1 [{:id 1 :label "Node 1"}
             {:id 2 :label "Node 2"}])

(def nodes
  [{:id "ServiceRoot" :label "Root"}])


(defn add-graph []
  (let [data {:nodes (-> (clj->js nodes) (js/vis.DataSet.))
              :edges (-> (clj->js {}) (js/vis.DataSet.))}]
    (clj->js data)))

(defn update-state [s m]
  (reset! s m))

(declare get-resource)

;{"@odata.context"
;               "/redfish/v1/$metadata#AccountService.AccountService",
; "@odata.id" "/redfish/v1/AccountService",
; "@odata.type" "#AccountService.v1_2_2.AccountService",
; "Accounts" {"@odata.id" "/redfish/v1/AccountService/Accounts"},
; "Id" "",
; "Name" "AccountService",
; "Roles" {"@odata.id" "/redfish/v1/AccountService/Roles"}}



(defn child-handler [response]
  (info "Calling child handler")
  (let [id (get response "@odata.id")
        [_ name] (str/split id #"/redfish/v1/(\w+)")
        node-state (:nodes @node-state)
        node (.get node-state name)]
    (.update node-state (clj->js {:id name :title (pr-str response)}))
    (swap! app-state assoc :selected response)))


;paths (select-keys response (remove #(re-find (js/RegExp. "@odata*") %)
;                                    (keys response)))]))

(defn error-handler [response]
  (info "Error getting resource " response))

(defn get-node-links [m]
  (info "get-node-links")
  (let [result (mapv #(get-resource (:link %) child-handler) m)]
    (debug result)))

(defn root-handler [response]
  (info "Calling root handler")
  (let [m (get response "value")
        node-map (mapv (fn [r]
                         (let [name (get r "name")
                               url (get r "url")]
                           {:id    name
                            :label name
                            :group "ServiceRoot"
                            :title name
                            :link  url})) m)
        edges (reduce
                (fn [v m]
                  (conj v {:from "ServiceRoot" :to (:id m)}))
                []
                node-map)
        root-map (conj node-map {:id "ServiceRoot" :label "ServiceRoot"})
        data {:nodes (-> (clj->js root-map) (js/vis.DataSet.))
              :edges (-> (clj->js edges) (js/vis.DataSet.))}
        _ (reset! node-state data)]))

(defn auth-header
  [user password]
  (str "Basic " (base64/encodeString (str user ":" password))))

(defn basic
  [realm user password]
  (fn [req]
    (update req
            :headers
            (partial merge {"WWW-Authenticate" (str "Basic realm=\"" realm "\"")
                            "Authorization"    (auth-header user password)}))))

(def credentials (basic "Fake Realm" "root" "calvin"))




(defn get-resource
  ([handler resource & auth]
   (let [resource (if (nil? resource) "/redfish/v1/odata" resource)
         service (str (get @app-state :url) resource)
         _ (debug "URL" service)
         options {:format          :json
                  :response-format :json
                  :handler         handler
                  :error-handler   error-handler}
         ;:headers {"WWW-Authenticate" (str "Basic realm=\"" "Fake" "\"")
         ;          "Authorization"    (auth-header "root" "calvin")}}
         ;options (if auth (assoc options :headers {"WWW-Authenticate" (str  "Basic realm=\"" "Fake" "\"")
         ;                                          "Authorization" (auth-header "root" "calvin")}
         ;options (merge options {:headers {"WWW-Authenticate" (str "Basic realm=\"" "Fake" "\"")
         ;                                  "Authorization"    (auth-header "root" "calvin")}})
         _ (pp "options" options)]
     (GET service options)))
  ([handler]
   (get-resource handler nil)))

(defn add-network
  [sel key]
  {:did-mount
   (fn [state]

     (letfn [(update-network [n o]
               (print "Update-Network")
               (let [network (get @app-state :network)]
                 (.setOptions network o)
                 (.setData network n)))]
       (let [[content-ref] (:rum/args state)
             _ (pp "node-state-init" content-ref)
             node-data (.getDataSet (.-nodes @node-state))
             ;edge-data (.getDataSet (.-edges @node-state))
             data {:nodes {}
                   :edges {}}
             options {:autoResize true
                      :height     "600px"
                      :nodes      {:shape "box"
                                   :fixed false}
                      :clickToUse true
                      :interaction
                                  {:hover        true
                                   :tooltipDelay 1}}
             network (js/vis.Network. (js/document.getElementById "network") data (clj->js options))
             _ (swap! app-state assoc :network network)
             _ (.on node-data "*" (fn [e o]
                                    (info "Network Update" e)))
             _ (.on network "click" (fn [e o]
                                      ;(.log js/console "Clicked Node " e)
                                      (let [node (first (.-nodes e))
                                            node-dataset (:nodes @content-ref)
                                            node-link (.-link (.get node-dataset node))
                                            _ (get-resource child-handler node-link)]
                                        (info "Clicked Node " (.get node-dataset node)))))]


         (add-watch content-ref key (fn [_ _ _ new-val]
                                      (do
                                        (debug "Found new-val " new-val)
                                        (update-network
                                          (clj->js new-val)
                                          (clj->js (conj
                                                     options
                                                     {:nodes
                                                      {:color  "green"
                                                       ;:scaling {:label true}
                                                       :shadow false}}))))))
         state)))

   :should-update
   (fn [old new] false)

   :will-unmount
   (fn [state]
     (let [[content-ref] (:rum/args state)]
       (remove-watch content-ref key))
     state)})

(defn tran-nodes []
  (clj->js (into [] @node-state)))


(defc network < rum/reactive
                (add-network "network" ::network)
  [content-ref]
  (rum/react node-state)
  [:div
   [:div {:id "network"}]])


(defc show-state < rum/reactive
  [state]
  (rum/react state)
  (let [s (if (map? @state)
            (into [] (deref state))
            @state)]
    [:div
     [:pre {:key "inspector-view"}
      (with-out-str (sort (pprint s)))]]))

(defc print-out < rum/reactive
  []
  (let [cursor (rum/cursor app-state :selected)]
    [:div.container
     [:pre (rum/react cursor)]]))

(declare input-component)

(defc home-page
  [app-state]
  (info "Loading... home-page")
  (update-state node-state (add-graph))
  (get-resource root-handler)
  [:div.container
   [:Row
    (input-component)
    (show-state app-state)]
   ;(show-state node-state)]
   [:Row
    (network node-state)]
   [:Row]])
;(print-out)]])



(defc input-component
  []
  [:div
   [:p "katfish: http://takagi:8765"]
   [:p "emulator http://localhost:5000"]
   [:p "14G https://172.29.209.35"]

   [:input {:type              "text"
            :allow-full-screen true
            :id                "comment"
            :value             (get @app-state :url)
            :class             ["input_active" "input_error"]
            :style             {:margin-left 5}
            :on-change         (fn [e]
                                 (do
                                   (swap! app-state assoc :url (.. e -target -value))))}]
   [:button {:label "Run" :on-click #(rum/mount (home-page app-state) (js/document.getElementById "app"))}]])


(rum/mount (home-page app-state) (js/document.getElementById "app"))


(defn on-js-reload [])
;; optionally touch your app-state to force rerendering depending on
;; your application
;; (swap! app-state update-in [:__figwheel_counter] inc)

