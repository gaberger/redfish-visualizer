(ns vis-1.bench)


;Table

;[:div.container]
;;[:div.box
;; [:section.header
;;  [:h5 "Node Configuration"]]
;; ;[:a.btn {:on-click #(reset! app-state true)} "Expand"]]
;; [:section.header
;;  (ui/table
;;    (ui/table-header
;;      (ui/table-row
;;        (ui/table-header-column "A")
;;        (ui/table-header-column "A")
;;        (ui/table-header-column "A")))
;;    (ui/table-body
;;      (ui/table-row
;;        (ui/table-row-column   "1")
;;        (ui/table-row-column   "2")
;;        (ui/table-row-column   "3"))
;;      (ui/table-row
;;        (ui/table-row-column   "1")
;;        (ui/table-row-column   "2")
;;        (ui/table-row-column   "3"))))]]]))


;(rum/defcs table-card < (rum/local {} ::form-state)
;                        rum/reactive
;  [state hosts form-data]
;  (let [form-state (::form-state state)
;        hostmap (get-compute @hosts)
;        dns-state (rum/cursor-in form-data [:dns-state])
;        user-state (rum/cursor-in form-data [:user-state])
;        host-state (rum/cursor-in form-data [:host-state])
;        interface-state (rum/cursor-in form-data [:interface-state])
;        action-state (rum/cursor-in form-state [:action-state])]
;    [:div
;     (when (= @action-state true)
;       (host-table-actions))
;     (ui/table {:selectable   true
;                :fixed-header true
;                :on-row-selection  #(reset! action-state true)}
;               (ui/table-header {:display-select-all  false
;                                 :adjust-for-checkbox true}
;                                (ui/table-row
;                                  (ui/table-header-column "Id")
;                                  (ui/table-header-column "Name")
;                                  (ui/table-header-column "Type")))
;               (ui/table-body {:show-row-hover       true
;                               :striped-rows         true
;                               :display-row-checkbox true}
;                              (map (fn [m]
;                                     (ui/table-row {:striped true}
;                                                   (ui/card
;                                                     (ui/card-text {:act-as-expander true}
;                                                                   (map #(ui/table-row-column %) m))
;                                                     (ui/card-text {:expandable true}
;                                                                   [:p "Child"]))))
;                                   hostmap)))
;     [:div
;

