;(ns vis-1.api
;  (:require
;    [vis-1.mock :as mock]
;    [cljs-http.client :as http]
;    [cljs.core.async :refer [<! >! take! put! chan buffer close! timeout]]
;    [rum.core :as rum]
;    [clojure.string :as str]
;    [cljs.pprint :as pprint]
;    [ajax.core :refer [ajax-request url-request-format json-response-format]])
;  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
;
;
;(def nr "http://127.0.0.1:1880")
;(def rhdapi "http://127.0.0.1:9090/api/2.0")
;(def imgsvc "http://localhost:7070")
;
;
;(defn make-network-device [interface]
;  (let [phy-state-v (:form-state (get-in interface [:phy-state]))
;        bond-state-v (:form-state (get-in interface [:bond-state]))
;        merged-state (into [] (concat phy-state-v bond-state-v))]
;    (mapv (fn [m]
;            (let [locmap {}
;                  devmap {}
;                  bonds (if (contains? m :bondinterface)
;                          (let [address (assoc-in locmap [:ipv4 :ipAddr] "0.0.0.0")
;                                netmask (assoc-in address [:ipv4 :netmask] "0.0.0.0")
;                                gateway (assoc-in netmask [:ipv4 :gateway] "0.0.0.0")
;                                bondinterface (assoc gateway :bondinterface (:bondinterface m))
;                                ismaster (assoc bondinterface :master (:isMaster? m))
;                                device (assoc ismaster :device (:device m))]
;                            device)
;                          (let
;                            [device (assoc devmap :device (:device m))
;                             address (assoc-in device [:ipv4 :ipAddr] (:ipaddress m))
;                             netmask (assoc-in address [:ipv4 :netmask] (:netmask m))
;                             gateway (assoc-in netmask [:ipv4 :gateway] (:gateway m))]
;                            gateway))]
;              bonds))
;          merged-state)))
;
;
;
;
;;{:device device
;; :ipv4   {:ipAddr  ipaddress
;;          :gateway gateway
;;          :netmask netmask
;;          :master  (get interface :master)}}))
;
;;
;;DEVICE=bond0
;;IPADDR=192.168.1.1
;;NETMASK=255.255.255.0
;;ONBOOT=yes
;;BOOTPROTO=none
;;USERCTL=no
;;NM_CONTROLLED=no
;;BONDING_OPTS="bonding parameters separated by spaces"
;;BONDING_OPTS="mode=active-backup miimon=100"
;;
;;DEVICE=ethX
;;BOOTPROTO=none
;;ONBOOT=yes
;;MASTER=bond0
;;SLAVE=yes
;;USERCTL=no
;;NM_CONTROLLED=no
;
;
;(defn clear-contents [ids]
;  (map (fn [id]
;         (let [element-name (name id)
;               _ (print (str element-name))
;               _ (print (.-value (js/document.getElementById (str element-name))))]
;           (set! (.-value (js/document.getElementById (str element-name))) nil)))
;       ids))
;
;(defn clear-by-id [e id]
;  (do
;    (.preventDefault e)
;    (.reset (.. e -target -value))))
;
;;(.. .reset (js/document.getElementById id)))
;
;
;(defn make-users [users]
;  (let [{:keys [username password1]} users]
;    {:name     username
;     :password password1}))
;
;
;(defn make-dns-servers [dns]
;  (let [{:keys [dnsserver]} dns]
;    dnsserver))
;
;(defn make-partitions [mount-point size fstype]
;  (vector
;    {:mountPoint mount-point
;     :size       (str size)
;     :fsType     fstype}))
;
;(defn make-rhel-workflow [& args]
;  (let [[kickstart osversion repo rootpassword hostname domain user-list dns-servers network-devices kvm? install-disk partitions subUser subPw subPool] args
;        ;isorepo (nth )
;        wf {:name "Graph.InstallRHEL"
;            :options
;                  {:defaults   {
;                                :version        osversion
;                                :repo           repo
;                                :rootPassword   rootpassword
;                                :hostname       hostname
;                                :domain         (str domain)
;                                :users          user-list
;                                ;:rootSshKey "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDJQ631/sw3D40h/6JfA+PFVy5Ofz6eu7caxbv0zdw4fTJsrFcOliHZTEtAvqx7Oa8gqSC6d7v61M0croQxtt1DGUcH2G4yhfdZOlK4pr5McwtX0T/APACdAr1HtP7Bt7u43mgHpUG4bHGc+NoY7cWCISkxl4apkYWbvcoJy/5bQn0uRgLuHUNXxK/XuLT5vG76xxY+1xRa5+OIoJ6l78nglNGrj2V+jH3+9yZxI43S9I3NOCl4BvX5Cp3CFMHyt80gk2yM1BJpQZZ4GHewkI/XOIFPU3rR5+toEYXHz7kzykZsqt1PtbaTwG3TX9GJI4C7aWyH9H+9Bt76vH/pLBIn rackhd@rackhd-demo",
;                                :dnsServers     dns-servers
;                                :networkDevices network-devices
;                                :kvm            (boolean kvm?)
;                                :subUser        subUser
;                                :subPw          subPw
;                                :subPool        subPool
;                                :installDisk    install-disk}
;                   :install-os {
;                                :installScript kickstart
;                                :profile       "install-centos.ipxe"}}}
;        ;./on-taskgraph/data/profiles/install-centos.ipxe - PXE banner
;        ;:installPartitions partitions}}}
;        _ (print "DEBUG: WF" wf)]
;    wf))
;
;
;
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
;
;
;(defn get-interfaces
;  "Retrieve interfaces. "
;  [app-state nodeid]
;  (let [mock @(rum/cursor app-state :mock)]
;    (print "DEBUG: Calling get-active-workflows for node id " nodeid)
;    (if (not mock)
;      (let [foo "bar"]
;        foo)
;      ;(let [url (str/join "/" [rhdapi "nodes" nodeid "workflows?active=true"])
;      ;      _ (print "send request to " url)
;      ;      nodekey (keyword nodeid)
;      ;      request {:with-credentials? false
;      ;               :timeout           5000}]
;      ;  (go
;      ;    (let [response (<! (http/get url request))]
;      ;      (if-not (:success response)
;      ;        (print "error in response")
;      ;        (swap! app-state assoc [:wf-alert] (count (:body response)))))))
;      ;(fn []
;      ;  (let [wfcount (count (:body response))]
;      ;    (print "WFCOUNT " wfcount)
;      ;    ;(swap! app-state assoc-in [:node] { nodekey {:status (:body response)}})
;      ;    (swap! app-state assoc [:wf-alert] wfcount)))))))
;      ;      #_(send-snackbar-message "get-models" "Error getting  models" true))))))
;      ;TODO fix
;      (swap! app-state assoc :interfaces mock/mock-interfaces))))
;
;(defn update-active-workflows
;  [app-state]
;  (let [coll (get-in @app-state [:nodes])
;        hosts (into [] (utils/get-compute coll))]
;    (map #(get-active-workflows app-state (:id %)) hosts)))
;
;(defn get-nodes
;  "Retrieve nodes. "
;  [app-state]
;  (let [mock @(rum/cursor app-state :mock)
;        nodes (rum/cursor-in app-state [:nodes])]
;    (if (not mock)
;      (let [url (str/join "/" [rhdapi "nodes"])
;            request {:with-credentials? false
;                     :timeout           5000}]
;        (go
;          (let [response (<! (http/get url request))]
;            ;_ (pprint/pprint response)]
;            (if-not (:success response)
;              (print "error in response")
;              (reset! nodes (:body response))))))
;      ;      #_(send-snackbar-message "get-models" "Error getting  models" true))))))
;      (reset! nodes mock/mock-nodes))))
;
;(defn get-isos
;  "Retrieve iso from image server. "
;  [app-state]
;  (let [mock (get-in @app-state [:mock])
;        iso (rum/cursor-in app-state [:iso])]
;    (if (not mock)
;      (let [url (str/join "/" [imgsvc "images"])
;            request {:with-credentials? false
;                     :timeout           5000}]
;        (go
;          (let [response (<! (http/get url request))]
;            (if-not (:success response)
;              (print "error in response")
;              (reset! iso (:body response))
;              #_(send-snackbar-message "get-models" "Error getting  models" true)))))
;      (reset! iso mock/mock-isos))))
;;
;
;;
;;(defn rediscover-node [node]
;;  (let [url (str/join "/" [rhdapi "workflows"])
;;        headers {:with-credentials? false}
;;        payload {:json-params
;;                 {:node    "Graph.Refresh.Immediate.Discover"
;;                  :options {:reset-at-start          {:nodeId node}
;;                            :create-default-pollers  {:nodeId node}
;;                            :generate-enclosure      {:nodeId node}
;;                            :generate-sku            {:nodeId node}
;;                            :reset-at-start          {:nodeId node}
;;                            :discovery-refresh-graph {:graphOptions {:target node}
;;                                                      :nodeId       node}
;;                            :nodeId                  node}}}
;;        request (merge headers payload)
;;        _ (print (.stringify js/JSON (clj->js query)))]
;;    (go
;;      (let [response (<! (http/post url request))]
;;        (js/console.log (:body response))))))
;
;
;;(defn get-interfaces
;;  "Retrieve nodes. "
;;  [app-state nodeid]
;;  (let [url (str/join "/" [rhdapi "nodes" nodeid "catalogs"])
;;        nodekey (keyword nodeid)
;;        request {:with-credentials? false
;;                 :timeout           5000}]
;;    (go
;;      (let [response (<! (http/get url request))
;;            _ (print response)]
;;        (if-not (:success response)
;;          (print "error in response")
;;          (print (utils/json->clj (:body response)))
;;          ;(swap! app-state assoc-in [:nodes :catalog nodekey] (js->clj (.parse js/JSON (:body response)) :keywordize-keys true))
;;          #_(send-snackbar-message "get-models" "Error getting  models" true))))))
;;
;
;(defn ajax-wrapper
;  [uri method params handler]
;  (ajax-request
;    {:uri             uri
;     :method          method
;     :params          params
;     :handler         handler
;     :format          (url-request-format)
;     :response-format (json-response-format {:keywords? true})}))
;
;(defn ajax-mixin [url method params key]
;  {:will-mount
;   (fn [state]
;     (let [*data (atom nil)
;           comp (:rum/react-component state)]
;       (ajax-wrapper
;         url
;         method
;         params
;         (fn [[ok response]]
;           (reset! *data response)
;           (rum/request-render comp)))
;       (assoc state key *data)))})
;
;(defn get-catalog
;  "Get Catalog Entry. "
;  [app-state nodeid catalog]
;  (let [url (str/join "/" [rhdapi "nodes" nodeid "catalogs" catalog])
;        nodekey (keyword nodeid)
;        request {:with-credentials? false
;                 :timeout           5000}]
;    (go
;      (let [response (<! (http/get url request))]
;        ;_ (pprint/pprint response)]
;        (if-not (:success response)
;          (print "error in response")
;          (swap! app-state assoc :catalog (:body response))
;          #_(send-snackbar-message "get-models" "Error getting  models" true))))))
;
;
;(defn post-workflow
;  "Post Workload for Execution"
;  [nodeid graph]
;  (let [url (str/join "/" [rhdapi "nodes" nodeid "workflows"])
;        ;(let [url (str/join "/" [nr "worflow" nodeid])
;        headers {:with-credentials? false}
;        query {:json-params graph}
;        request (merge headers query)
;        _ (print (.stringify js/JSON (clj->js query)))]
;    (go
;      (let [response (<! (http/post url request))]
;        (js/console.log (:body response))))))
;
;
;(defn post-cancel-workflow
;  "Cancel Workload for Execution"
;  [nodeid]
;  (let [url (str/join "/" [rhdapi "nodes" nodeid "workflows/actions"])
;        ;(let [url (str/join "/" [nr "worflow" nodeid])
;        headers {:with-credentials? false}
;        body {:json-params {:command "cancel"
;                            :options {}}}
;
;        request (merge headers body)
;        _ (print (.stringify js/JSON (clj->js body)))]
;    (go
;      (let [response (<! (http/post url request))]
;        (js/console.log (:body response))))))
;
;
;(defn post-task-upload
;  "Upload task"
;  [task]
;  (let [url (str/join "/" [rhdapi "workflows/tasks"])
;        headers {:with-credentials? false}
;        payload {:json-params task}
;        request (merge headers payload)]
;    (go
;      (let [response (<! (http/put url request))]
;        (if-not (:success response)
;          (js/console.log (str (clj->js response))))))))
;
;
;(defn post-graph-upload
;  "Upload task"
;  [graph]
;  (let [url (str/join "/" [rhdapi "workflows/graphs"])
;        headers {:with-credentials? false}
;        payload {:json-params graph}
;        request (merge headers payload)]
;    (go
;      (let [response (<! (http/put url request))]
;        (if-not (:success response)
;          (js/console.log response))))))
;
;
;(defn get-keys []
;  (into [] (remove nil? (js->clj (.keys js/Object js/localStorage)))))
;
;(defn put-object [key obj]
;  (let [json (clj->js obj)
;        _ (print "INFO: Persist object in session store using key: " key)]
;    (.setItem js/localStorage key (.stringify js/JSON json))))
;
;
;(defn get-object [key]
;  (let [json (.getItem js/localStorage key)
;        _ (print "INFO: Retrieve object from session store using key: " key)]
;    (js->clj (.parse js/JSON json) :keywordize-keys true)))
;
;
;(defn load-data [app-state]
;  (print "DEBUG: Loading Data")
;  (get-nodes app-state)
;  (get-isos app-state))
;;(post-task-upload net-inventory-task)
;;(post-graph-upload  net-inventory-graph))
;;(let [nodes (get-compute (@app-state :nodes))]
;;  (map (fn [node]
;;         (post-workflow (:id node) make-inventory-graph)) nodes)
;
