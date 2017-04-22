(ns calender.db
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [clojure.walk]
            [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.cypher :as cy]
            [clojurewerkz.neocons.rest.constraints :as cn]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clojurewerkz.neocons.rest.relationships :as nrl]))

(defn string->key [data]
  (clojure.walk/keywordize-keys data))

(defn extract-id [node]
  (-> node
     (string->key)
     (:n)
     (:metadata)
     (:id)))

(defn get-node-by-id [id conn]
  (nn/get conn id))

(defn create-date-node [body]
  (log/debug "Create date node called")
  (try
    (let [{:keys [month data duration date start-time end-time]} body
          conn (nr/connect "http://test:test123@localhost:7474/db/data/")
          node (nn/create conn {:data data :dur duration :start [start-time] :end [end-time]})
          fetch-data (first (cy/tquery conn "MATCH (n) WHERE n.value = {month} RETURN n" {:month month}))
          month-node (-> fetch-data
                         (extract-id)
                         (get-node-by-id conn))
          rel  (nrl/create conn month-node node :date {:value date})]
          (nl/add conn node "Date")
        (log/infof "Created the relationship between month: %s and date: %s. Relation %s" month date rel))
    (catch Exception e (log/error e))))

(defn create-node [conn label value]
  (log/info (format "creating node for label: %s and value %s" label value))
  (try
    (let [node (nn/create conn {:value value})]
      (nl/add conn node "Month")
      (log/info "Node successfully created"))
  (catch Exception ex (log/error "Node already exists"))))

(defn -main [& args]
  (let [conn (nr/connect "http://test:test123@localhost:7474/db/data/")
        months ["JAN" "FEB" "MAR" "APR" "MAY" "JUNE" "JULY" "AUG" "SEPT" "OCT" "NOV" "DEC"]]
    (dorun (map #(create-node conn "MONTH" %) months))))


;;     (cn/drop-constraint conn "Month" :value)
;;     (println (:data (:n (clojure.walk/keywordize-keys s))))))
;;   (let [tokens (AuthTokens/basic "neo4j" "neo4j")
;;         driver (GraphDatabase/driver "bolt://localhost:7474" tokens)
;;         session(.session driver)]
;;         (try
;;           (let [txn (.beginTransaction session)]
;;                 (.run txn "CREATE (:Person {name: {name}})"
;;   (.parameters Values "name" "King" ))
;;                 (.success txn)
;;           (.close driver)))))
;; k (cy/tquery conn "MATCH (n) WHERE n.name = {name} RETURN n" {:name "Kean"})
;;         s (first k)
