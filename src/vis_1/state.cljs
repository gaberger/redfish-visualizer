(ns vis-1.state)


(defonce app-state (atom {:login         nil
                          :accept-login  nil
                          :debug-state   false
                          :page-state    nil
                          :drawer-state  false
                          :inspect-state false
                          :expand        false
                          :mock          false
                          :save-profile  false
                          :wf-alert      0
                          :eventdb       []}))

;(defonce app-state (atom {}))

(defn add-graph []
  {:nodes []
   :edges []})

(def node-state (atom (add-graph)))