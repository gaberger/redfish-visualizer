(ns vis-1.utils
  (:require
    [rum.core :as rum]
    [vis-1.state :refer [node-state app-state]]
    [cemerick.url :refer [url url-encode]]
    [cljs.pprint :as pprint]
    [taoensso.timbre :as timbre
     :refer-macros [log trace debug info warn error fatal report
                    logf tracef debugf infof warnf errorf fatalf reportf
                    spy get-env]]
    [clojure.string :as str]))


(defn pp [t r]
  (print t)
  (.log js/console r))


(defn node-count []
  (let [node-dataset (:nodes @node-state)
        node-count (count node-dataset)]
    (info "Node Count: " node-count)
    node-count))

(defn get-compute [coll]
  (filter #(= (:type %) "compute")
          (mapv (fn [node]
                  (select-keys node [:id :name :type])) coll)))

(defn json->clj [string]
  (try
    (print string)
    (js->clj (.parse js/JSON string) :keywordize-keys true)
    (catch js/SyntaxError err
      (print "Error: Error parsing object " err))))

(defn add-node-dataset [& v]
  (info "add-node-dataset")
  (let [[id label title link from] v
        _ (info "input " (str/join " " [id label from title link]))
        node-state (rum/cursor node-state :nodes)
        edge-state (rum/cursor node-state :edges)
        nodes @node-state
        edges @edge-state
        node (clj->js {:id id :text label :label label :title title :link link})
        edge (clj->js {:from from :to id})]
    (swap! node-state conj node)
    (when (not (nil? from))
      (do
        (swap! edge-state conj edge)))))


(defn update-service-root [m]
  (info "update-service-root")
  (let [edge-state (rum/cursor node-state :edges)
        node-state (rum/cursor node-state :nodes)]
    (reduce (fn [a coll]
              (let [name (get coll "name")
                    link (get coll "url")]
                (swap! node-state conj {:id name :label name :link link})
                (swap! edge-state conj {:from name :to "ServiceRoot"})))
            []
            m)))


(defn update-network [n o]
  (info "update-network")
  (.log js/console n)
  (let [network (get @app-state :network)]
    (.log js/console network)
    (.setOptions network o)
    (.setData network n)))

(defn add-nodes [m root]
  (info "add-nodes")
  (map (fn [r]
         (let [name (key r)
               link (get (val r) "@odata.id")]
           (info "adding node " name)
           (add-node-dataset name name name link)))
       m))
;