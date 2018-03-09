(ns vis-1.tasks)


(def net-inventory-task
  {
   :friendlyName   "interface inventory"
   :injectableName "Task.Ssh.Lshwnet"
   :implementsTask "Task.Base.Ssh"
   :options        {:commands
                    {:catalog {:source "lshwnet"
                               :format "json"}
                     :command "lshw --class network -json"}}
   :properties     {}})


(def net-inventory-graph
  {
   :injectableName "Graph.Ssh.Inventory",
   :friendlyName   "Run some tasks"
   :tasks          [{:label    "validate"
                     :taskName "Task.Get.Inventory"}
                    {:label    "exec"
                     :taskName "Task.Ssh.Lshwnet"}]})



;(defn node-discover-graph [nodeid]
;  {
;   :name    "Graph.Refresh.Immediate.Discovery",
;   :options {
;             :create-default-pollers  {:nodeId nodeid}
;             :discovery-refresh-graph {:graphOptions {:target nodeid}
;                                       :nodeId       nodeid}
;             :generate-enclosure      {:nodeId nodeid}
;             :generate-sku            {:nodeId nodeid}
;             :reset-at-start          {:nodeId nodeid}
;             :run-sku-graph           {:nodeId nodeid}}})



(def make-inventory-graph
  {:name    "Graph.Ssh.Inventory"
   :options {:validate
                       {:users  [{:name     "root"
                                  :password "password"}]
                        :config {:sshSettings
                                 {:username "root"
                                  :password "password"}}}

             :exec
                       {:users  [{:name     "root"
                                  :password "password"}]
                        :config {:sshSettings {:username "root"
                                               :password "password"}}}

             :defaults {}}})