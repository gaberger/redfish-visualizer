(ns vis-1.mock)

(def mock-templates
  ["a" "b" "c"])

(def mock-isos
  [{:id      "d22bbd2d-9610-4390-8a5c-a2c8757cc285"
    :name    "rhel",
    :version "7.3"
    :status  "OK"
    :iso     "rhel-server-7.3-x86_64-dvd.iso"}
   {:id      "e28f1f5d-873f-4df6-8f0d-1da8dc4c2083"
    :name    "rhel"
    :version "7.4"
    :status  "OK"
    :iso     "rhel-server-7.4-x86_64-dvd.iso"}
   {:id      "e28f1f5d-873f-4df6-8f0d-1da8dc4c2083"
    :name    "ubuntu"
    :version "14.04"
    :status  "OK"
    :iso     "rhel-server-7.4-x86_64-dvd.iso"}
   {:id      "e28f1f5d-873f-4df6-8f0d-1da8dc4c2083"
    :name    "ubuntu",
    :version "16.04",
    :status  "OK",
    :iso     "rhel-server-7.4-x86_64-dvd.iso"}
   {:id      "e28f1f5d-873f-4df6-8f0d-1da8dc4c2083"
    :name    "ESXi",
    :version "6.5",
    :status  "OK",
    :iso     "rhel-server-7.4-x86_64-dvd.iso"}])

(def mock-nodes
  [{:ibms         [],
    :tags         "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/tags",
    :identifiers  ["24:6e:96:6a:58:dc"],
    :relations
                  [{:relationType "enclosedBy",
                    :info         nil,
                    :targets      ["5a0e1cec2cee210100c0b3c8"]}],
    :name         "24:6e:96:6a:58:dc",
    :autoDiscover false,
    :catalogs     "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/catalogs",
    :pollers      "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/pollers",
    :type         "compute",
    :workflows    "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/workflows",
    :sku          nil,
    :id           "5a0e1c1a2cee210100c0b3ac",
    :obms
                  [{:service "ipmi-obm-service",
                    :ref     "/api/2.0/obms/5a0e1cf82cee210100c0b3d5"}]}
   {:ibms         [],
    :tags         "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/tags",
    :identifiers  ["24:6e:96:6a:58:dd"],
    :relations
                  [{:relationType "enclosedBy",
                    :info         nil,
                    :targets      ["5a0e1cec2cee210100c0b3c9"]}],
    :name         "24:6e:96:6a:58:dd",
    :autoDiscover false,
    :catalogs     "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/catalogs",
    :pollers      "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/pollers",
    :type         "compute",
    :workflows    "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/workflows",
    :sku          nil,
    :id           "5a0e1c1a2cee210100c0b3ad",
    :obms
                  [{:service "ipmi-obm-service",
                    :ref     "/api/2.0/obms/5a0e1cf82cee210100c0b3d5"}]}
   {:ibms         [],
    :tags         "/api/2.0/nodes/5a0e1cec2cee210100c0b3c8/tags",
    :identifiers  [],
    :relations
                  [{:relationType "encloses",
                    :info         nil,
                    :targets      ["5a0e1c1a2cee210100c0b3ac"]}],
    :name         "Enclosure Node 1P69HK2",
    :autoDiscover false,
    :catalogs     "/api/2.0/nodes/5a0e1cec2cee210100c0b3c8/catalogs",
    :pollers      "/api/2.0/nodes/5a0e1cec2cee210100c0b3c8/pollers",
    :type         "enclosure",
    :workflows    "/api/2.0/nodes/5a0e1cec2cee210100c0b3c8/workflows",
    :id           "5a0e1cec2cee210100c0b3c8",
    :obms         []}
   {:ibms         [],
    :tags         "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/tags",
    :identifiers  ["24:6e:96:6a:58:de"],
    :relations
                  [{:relationType "enclosedBy",
                    :info         nil,
                    :targets      ["5a0e1cec2cee210100c0b3c9"]}],
    :name         "24:6e:96:6a:58:de",
    :autoDiscover false,
    :catalogs     "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/catalogs",
    :pollers      "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/pollers",
    :type         "compute",
    :workflows    "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/workflows",
    :sku          nil,
    :id           "5a0e1c1a2cee210100c0b3ae",
    :obms
                  [{:service "ipmi-obm-service",
                    :ref     "/api/2.0/obms/5a0e1cf82cee210100c0b3d5"}]}
   {:ibms         [],
    :tags         "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/tags",
    :identifiers  ["24:6e:96:6a:58:df"],
    :relations
                  [{:relationType "enclosedBy",
                    :info         nil,
                    :targets      ["5a0e1cec2cee210100c0b3c8"]}],
    :name         "24:6e:96:6a:58:df",
    :autoDiscover false,
    :catalogs     "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/catalogs",
    :pollers      "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/pollers",
    :type         "compute",
    :workflows    "/api/2.0/nodes/5a0e1c1a2cee210100c0b3ac/workflows",
    :sku          nil,
    :id           "5a0e1c1a2cee210100c0b3af",
    :obms
                  [{:service "ipmi-obm-service",
                    :ref     "/api/2.0/obms/5a0e1cf82cee210100c0b3d5"}]}])


(def mock-workflows {})

(def mock-interfaces
  ["em1" "em2" "em3" "em4" "p1p1" "p1p2" "p2p1" "p2p2"])



(def i {:row-selected    0,
        :interface-state {:row-state {:type "VLAN"},}
        :bond-state      {:row-state  {:type nil},
                          :form-state [{:device     "bond0",
                                        :ipaddress  "1.1.1.1",
                                        :netmask    "255.255.255.0",
                                        :gateway    "1.1.1.254",
                                        :type       "Bond",
                                        :bondparams "802.3ad miimon=100"},]}
        :phy-state       {:row-state {}, :form-state [{:device        "p2p2",
                                                       :bondinterface "bond0",
                                                       :isMaster?     false}
                                                      {:device        "p2p1",
                                                        :bondinterface "bond0",
                                                        :isMaster?     false}]}})