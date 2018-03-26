(ns redfish_visualizer.core
  (:require
    [redfish_visualizer.state :refer [app-state]]
    [redfish_visualizer.components :refer [home-page]]
    [rum.core :as rum :refer [defc]]))

(enable-console-print!)

(rum/mount (home-page app-state) (js/document.getElementById "app"))

