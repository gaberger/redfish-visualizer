(defproject redfish-visualizer "0.1.0-SNAPSHOT"
  :description "Sample Application to Visualize RedFish endpoint"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.7.1"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.908"]
                 [org.clojure/core.async  "0.4.474"]
                 [cljsjs/react-dom "15.6.2-4"]
                 [cljsjs/react "15.6.2-4"]
                 [cljsjs/material-ui "0.19.2-0"]
                 [cljs-react-material-ui "0.2.48"]
                 [rum "0.10.8" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [cljs-ajax "0.7.3"]
                 [cljsjs/vis "4.20.1-0"]
                 [com.taoensso/timbre "4.10.0"]
                 [rum-reforms "0.4.3"]
                 [cljs-http "0.1.41"]
                 [medley "1.0.0"]
                 [com.cemerick/url "0.1.1"]]

  :plugins [[lein-figwheel "0.5.15"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                :figwheel {:on-jsload "redfish_visualizer.core/on-js-reload"
                           :open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main redfish_visualizer.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/redfish_visualizer.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/redfish_visualizer.js"
                           :main redfish_visualizer.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {
             :css-dirs ["resources/public/css"]} ;; watch and update CSS

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.9"]
                                  [figwheel-sidecar "0.5.15"]
                                  [com.cemerick/piggieback "0.2.2"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
