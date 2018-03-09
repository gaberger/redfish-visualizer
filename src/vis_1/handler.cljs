(ns vis-1.handler
  (:require
            [cemerick.url :refer [url url-encode]]
            [taoensso.timbre :as timbre :refer [info]]
            [ajax.core :refer [GET POST ajax-request json-request-format json-response-format]]
            [vis-1.state :refer [app-state]]
            [vis-1.utils :refer [pp add-nodes update-service-root add-node-dataset node-count]]))




(defn root-handler [response]
  (info "Calling root handler")
  (let [m (get response "value")]
    ;(js* "debugger;")
    (update-service-root m)))


(defn child-handler [response]
  (info "Calling child handler")
  (let [id (get response "@odata.id")
        context (get response "@odata.context")
        paths (select-keys response (remove #(re-find (js/RegExp. "^@odata*|Id|Name") %) (keys response)))
        _ (info "Found paths " paths)]
    (add-nodes paths context)
    (swap! app-state assoc :selected response)))


(defn error-handler [response]
  (info "Error getting resource " response))

(defn get-resource
  []
  (info "get-resource")
  (let [resource (get @app-state :url)
        url (url resource)
        [handler resource] (if (empty? (:path url))
                             [root-handler (-> url (assoc :path "/redfish/v1/odata") str)]
                             [child-handler resource])
        _ (when (= (node-count) 0) (add-node-dataset "ServiceRoot" "ServiceRoot" "ServiceRoot" "/redfish/v1/odata" nil))
        _ (info "URL" resource)
        options {:format          :json
                 :response-format :json
                 :handler         handler
                 :error-handler   error-handler}]
    ;:headers {"WWW-Authenticate" (str "Basic realm=\"" "Fake" "\"")
    ;          "Authorization"    (auth-header "root" "calvin")}}]
    ;options (if auth (assoc options :headers {"WWW-Authenticate" (str  "Basic realm=\"" "Fake" "\"")
    ;                                          "Authorization" (auth-header "root" "calvin")}
    ;options (merge options {:headers {"WWW-Authenticate" (str "Basic realm=\"" "Fake" "\"")
    ;                                  "Authorization"    (auth-header "root" "calvin")}})
    (GET resource options)))

;paths (select-keys response (remove #(re-find (js/RegExp. "@odata*") %)
;                                    (keys response)))]))



