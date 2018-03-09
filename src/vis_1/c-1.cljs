;(ns vis-1.core1
;  (:require
;    ;[antizer.rum :as ant]
;    ;[antizer.core :as ac]
;    ;[clojure.walk :refer [keywordize-keys]]
;    [rum.core :as rum :refer [defc]]
;    [ajax.core :refer [GET POST ajax-request json-request-format json-response-format]]
;    [cljsjs.vis]
;    [cljs.pprint :refer [pprint]]
;    [clojure.string :as str]
;    [goog.crypt.base64 :as base64]
;    [cemerick.url :refer [url url-encode]]
;    [taoensso.timbre
;     :refer-macros [info debug spy]]))
;
;(enable-console-print!)
;
;(info "test")
;
;;TODO Use odata as root odata
;
;(defn pp [t r]
;  (print t)
;  (.log js/console r))
;
;(defonce app-state (atom {}))
;
;(defn add-graph []
;  {:nodes []
;   :edges []})
;
;;Initialize graph with empty dataset
;(def node-state (atom (add-graph)))
;
;;(add-watch node-state :watcher
;;           (fn [key atom old-state new-state]
;;             (prn "-- Atom Changed --")
;;             (prn "key" key)
;;             (prn "atom" atom)
;;             (prn "old-state" old-state)
;;             (prn "new-state" new-state)))
;
;(defn node-count []
;  (let [node-dataset (:nodes @node-state)
;        node-count (count node-dataset)]
;    (info "Node Count: " node-count)
;    node-count))
;;
;;(def nodes
;;  [{:id "ServiceRoot" :label "Root"}])
;
;(defn json->clj [string]
;  (try
;    (js->clj (.parse js/JSON string) :keywordize-keys true)
;    (catch js/SyntaxError err
;      (print "Error: Error parsing object " err))))
;
;
;
;
;(defn add-node-dataset [& v]
;  (info "add-node-dataset")
;  (let [[id label title link from] v
;        _ (info "input " (str/join " " [id label from title link]))
;        node-state (rum/cursor node-state :nodes)
;        edge-state (rum/cursor node-state :edges)
;        nodes @node-state
;        edges @edge-state
;        node (clj->js {:id id :text label :label label :title title :link link})
;        edge (clj->js {:from from :to id})]
;    (swap! node-state conj node)
;    (when (not (nil? from))
;      (do
;        (swap! edge-state conj edge)))))
;
;
;(defn add-nodes [m root]
;  (info "add-nodes")
;  (map (fn [r]
;         (let [name (key r)
;               link (get (val r) "@odata.id")]
;           (info "adding node " name)
;           (add-node-dataset name name name link)))
;       m))
;;
;
;(defn update-state [s m]
;  (reset! s m))
;
;(declare get-resource)
;
;;{"@odata.context"
;;               "/redfish/v1/$metadata#AccountService.AccountService",
;; "@odata.id" "/redfish/v1/AccountService",
;; "@odata.type" "#AccountService.v1_2_2.AccountService",
;; "Accounts" {"@odata.id" "/redfish/v1/AccountService/Accounts"},
;; "Id" "",
;; "Name" "AccountService",
;; "Roles" {"@odata.id" "/redfish/v1/AccountService/Roles"}}
;
;
;
;;(defn get-node-links [m]
;;  (info "get-node-links")
;;  (let [result (mapv #(get-resource (:link %) child-handler) m)]
;;    (debug result)))
;
;
;(defn update-service-root [m]
;  ;(info "update-service-root")
;  (let [edge-state (rum/cursor node-state :edges)
;        node-state (rum/cursor node-state :nodes)]
;    (reduce (fn [a coll]
;              (let [name (get coll "name")
;                    link (get coll "url")]
;                (swap! node-state conj {:id name :label name :link link})
;                (swap! edge-state conj {:from name :to "ServiceRoot"})))
;            []
;            m)))
;
;(defn root-handler [response]
;  (info "Calling root handler")
;  (let [m (get response "value")]
;    ;(js* "debugger;")
;    (update-service-root m)))
;
;(defn auth-header
;  [user password]
;  (str "Basic " (base64/encodeString (str user ":" password))))
;
;(defn basic
;  [realm user password]
;  (fn [req]
;    (update req
;            :headers
;            (partial merge {"WWW-Authenticate" (str "Basic realm=\"" realm "\"")
;                            "Authorization"    (auth-header user password)}))))
;
;(def credentials (basic "Fake Realm" "root" "calvin"))
;
;
;
;
;(defn get-resource
;  []
;  (let [resource (get @app-state :url)
;        url (url resource)
;        [handler resource] (if (empty? (:path url))
;                             [root-handler (-> url (assoc :path "/redfish/v1/odata") str)]
;                             [child-handler resource])
;        _ (when (= (node-count) 0) (add-node-dataset "ServiceRoot" "ServiceRoot" "ServiceRoot" "/redfish/v1/odata" nil))
;        _ (info "URL" resource)
;        options {:format          :json
;                 :response-format :json
;                 :handler         handler
;                 :error-handler   error-handler}]
;                 ;:headers {"WWW-Authenticate" (str "Basic realm=\"" "Fake" "\"")
;                 ;          "Authorization"    (auth-header "root" "calvin")}}]
;    ;options (if auth (assoc options :headers {"WWW-Authenticate" (str  "Basic realm=\"" "Fake" "\"")
;    ;                                          "Authorization" (auth-header "root" "calvin")}
;    ;options (merge options {:headers {"WWW-Authenticate" (str "Basic realm=\"" "Fake" "\"")
;    ;                                  "Authorization"    (auth-header "root" "calvin")}})
;    (GET resource options)))
;;([handler]
;; (get-resource handler nil)))
;
;;(add-watch node-state :watcher
;;           (fn [key atom old-state new-state]
;;             (prn "-- Atom Changed --")
;;             (prn "key" key)
;;             (prn "atom" atom)
;;             (prn "old-state" old-state)
;;             (prn "new-state" new-state)))
;
;(defn update-network [n o]
;  (info "update-network")
;  (.log js/console n)
;  (let [network (get @app-state :network)]
;    (.log js/console network)
;    (.setOptions network o)
;    (.setData network n)))
;
;(defn add-network
;  [sel]
;  {:did-mount
;   (fn [state]
;     (let [[content-ref] (:rum/args state)
;           _ (info "node-state-init")
;           uri-state (rum/cursor app-state :url)
;           data {:nodes @node-state
;                 :edges @node-state}
;           options {:autoResize true
;                    ;:layout {
;                    ;         :randomSeed 41154115}
;                    :height     "600px"
;                    :nodes      {:shape "box"
;                                 :fixed false}
;                    :physics
;                                {:solver "repulsion"}
;                    :interaction
;                                {:hover                true
;                                 :dragNodes            true
;                                 :navigationButtons    true
;                                 ;:selectConnectedEdges false
;                                 :tooltipDelay         1}}
;           network (js/vis.Network. (js/document.getElementById sel) data (clj->js options))
;           _ (swap! app-state assoc :network network)
;           _ (.on (js/vis.DataSet. @node-state) "*" (fn [e o]
;                                                      (info "Network Update" e o)))
;           ;(update-network o (clj->js options))))
;           _ (.on network "click" (fn [e o]
;                                    (let [node (first (.-nodes e))
;                                          uri @uri-state
;                                          node-dataset (js/vis.DataSet. (clj->js (:nodes @node-state)))
;                                          node (.get node-dataset node)
;                                          node-link (.-link node)]
;                                      (reset! uri-state (str (url uri node-link)))
;                                      (get-resource)
;                                      (info "Clicked Node " node-link))))]
;       (add-watch node-state ::node-state (fn [_ _ _ new-val]
;                                            (do
;                                              (info "Found new-val " new-val)
;                                              (update-network
;                                                (clj->js new-val)
;                                                (clj->js (conj
;                                                           options
;                                                           {:nodes  {:color  "blue"
;                                                                     :shadow false
;                                                                     :size   60}})))))))
;
;
;     ;         :hierarchical {
;     ;                        :direction "LR"
;     ;                        :enabled true}}})))))))
;     state)
;
;   :should-update
;   (fn [old new] false)
;
;   :will-unmount
;   (fn [state]
;     (let [[content-ref] (:rum/args state)]
;       (remove-watch content-ref key))
;     state)})
;
;
;(defc network < rum/reactive
;                (add-network "network")
;  [content-ref]
;  (rum/react node-state)
;  [:div
;   [:div {:id "network"}]])
;
;(defc show-state < rum/reactive
;  [state]
;  (rum/react state)
;  (let [s (if (map? @state)
;            (into [] (deref state))
;            @state)]
;    [:div
;     [:pre {:key "inspector-view"}
;      (with-out-str (pprint (js->clj s)))]]))
;
;(defc print-out < rum/reactive
;  []
;  (let [cursor (rum/cursor app-state :selected)]
;    [:div.container
;     [:pre (rum/react cursor)]]))
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
;
;(defc home-page
;  [app-state]
;  (info "Loading... home-page")
;  ;(update-state node-state (add-graph))
;  ;(get-resource root-handler)
;  [:div.container
;   [:Row
;    (input-component)
;    (show-state app-state)]
;    ;(show-state node-state)]
;   [:Row
;    (network node-state)]
;   [:Row]])
;;(print-out)]])
;(rum/mount (home-page app-state) (js/document.getElementById "app"))
;
;
;(defn on-js-reload [])
;;; optionally touch your app-state to force rerendering depending on
;;; your application
;;; (swap! app-state update-in [:__figwheel_counter] inc)
;
