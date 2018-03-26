(ns redfish_visualizer.state)


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


(defn add-graph []
  {:nodes []
   :edges []})


(defonce node-state (atom (add-graph)))