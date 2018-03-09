(ns vis-1.core
  (:require
    [vis-1.state :refer [app-state]]
    [vis-1.components :refer [home-page]]
    [rum.core :as rum :refer [defc]]))

(enable-console-print!)

(rum/mount (home-page app-state) (js/document.getElementById "app"))

