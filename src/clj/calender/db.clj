(ns calender.db
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [clojure.walk]
            [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.cypher :as cy]
            [clojurewerkz.neocons.rest.constraints :as cn]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clojurewerkz.neocons.rest.paths :as np]
            [clojurewerkz.neocons.rest.relationships :as nrl]))

(defn string->key [data]
  (clojure.walk/keywordize-keys data))

(defn extract-id [node]
  (log/info node)
  (-> node
     (string->key)
     (:n)
     (:metadata)
     (:id)))

(defn get-node-by-id [id conn]
  (nn/get conn id))

(defn get-node [data conn]
  (-> data
    (extract-id)
    (get-node-by-id conn)))

(defn get-month-node [month conn]
  (let [fetch-data (first (cy/tquery conn "MATCH (n) WHERE n.value = {month} RETURN n" {:month month}))]
    (get-node fetch-data conn)))

(defn get-day-node [day conn]
  (let [fetch-data (first (cy/tquery conn "MATCH (n) WHERE n.date = {date} RETURN n" {:date (str day)}))]
    (when-not (nil? fetch-data)
      (get-node fetch-data conn))))

(defn create-node [conn property label]
 (nn/create conn property))

(defn create-relationship [from to relation property conn]
  (log/info to)
  (nrl/create conn from to relation property))

(defn date-exist? [month-node date-node conn]
  (log/info "Exist")
  (log/info (nil? date-node))
  (let []
    (cond (nil? date-node) false
      :else (np/exists-between? conn (:id month-node) (:id date-node) :relationships [{:direction "out" :type "date"}]))))

(defn add-label [node conn label]
  (nl/add conn node label))

(defn create [body month-node conn]
  (log/info "node")
  (let [ {:keys [month data duration date start-time end-time]} body
         properties {:date date :data data :dur duration :startTime [start-time] :endTime [end-time]}
         node (create-node conn properties "Date")]
         (log/info "FUCK")
         (add-label node conn "Date")
         (create-relationship month-node node :date {:value date} conn)))

(defn update-node [body node month conn]
  (log/info "Updating node")
  (let [{:keys [start-time end-time]} body
        {:keys [dur data date startTime endTime]} (:data node)
        update-start (conj startTime start-time)
        update-end (conj endTime end-time)]
        (log/info node)
        (log/info (:id node))
        (log/info update-start)
        (nn/update conn (:id node) {:date date :data data :dur dur :startTime update-start :endTime update-end})))

(defn create-date-node [body]
  (log/info "Create date node called")
  (try
    (let [{:keys [month date]} body
           conn (nr/connect "http://test:test123@localhost:7474/db/data/")
           month-node (get-month-node month conn)
           date-node  (get-day-node date conn)
           exist (date-exist? month-node date-node conn)]
           (log/info date-node)
           (log/info exist)
           (cond
               exist (update-node body date-node month-node conn)
              :else   (create body month-node conn)))
        (catch Exception e (log/error e))))

(defn create-month-node [conn label value]
  (log/info (format "Creating node for label: %s and value %s" label value))
  (try
      (let [node (create-node conn {:value value} "Month")]
        (add-label node conn "Month")
        (log/info "Node successfully created"))
  (catch Exception ex (log/error "Node already exists"))))

(defn -main [& args]
  (let [conn (nr/connect "http://test:test123@localhost:7474/db/data/")
        months ["JAN" "FEB" "MAR" "APR" "MAY" "JUNE" "JULY" "AUG" "SEPT" "OCT" "NOV" "DEC"]]
    (dorun (map #(create-month-node conn "MONTH" %) months))))




;; (np/exists-between? conn (:id month-node) (:id date-node) :relationships [{:direction "out" :type "date"}])
;;     (cy/tquery conn "MATCH (n:Month)-[rel:date]->(d:Date) WHERE d.date ={date} AND n.value={month} RETURN d" {:date (:date date-node) :month (:value month-node)})
;; conn (nr/connect "http://test:test123@localhost:7474/db/data/")
;;           node (create-node conn {:date date :data data :dur duration :start-time [start-time] :end-time [end-time]} "Date")
;;           month-node (get-month-node month conn)
;;           rel  (create-relationship month-node node :date {:value date} conn)]
;;         (log/infof "Created the relationship between month: %s and date: %s. Relation %s" month date rel))

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
