(ns calender.state
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [goog.string :as gstring]
              [goog.string.format :as format]
              [clojure.string :as str]))

(defn add-id []
  (let [ids
    (into []
      (map
        (fn [i] {:entry {:data nil :id (+ i 1) :start nil :end nil :duration nil :click false}})
          (range 31)))] ids))

(defn initializer-state []
  (.log js/console "state called")
   (let [month-array '(:JAN :FEB :MAR :APR :MAY :JUNE :JULY :AUG :SEPT :OCT :NOV :DEC)
         keyword-array (into [] (map keyword month-array))
         data (into {}
                (map
                  (fn[i] {i (add-id)}) month-array))] data))

;; (defonce view-pop (reagent/atom false))
;; (defonce current-month (reagent/atom "FEB"))
;; ;; (defonce value (reagent/atom ""))
;; ;; (defonce display (reagent/atom "display"))
;; ;; (defonce nums (reagent/atom 0))
;; ;; (defonce day (reagent/atom "SUNDAY"))
;; ;; (defonce remainder (reagent/atom 0))
;; ;; (defonce days-data (reagent/atom (initializer-state)))
;; ;; (defonce enteries (reagent/atom []))
;; ;; (defonce test-entry (reagent/atom {}))
;; ;; (defonce curr-data (reagent/atom nil))
;; ;; (defonce counter (reagent/atom 0))
;; ;; (defonce last-row (reagent/atom 0))
;; ;; (defonce empty-row (reagent/atom (get days (keyword @day))))

(defn initial-state []
  (let [days-data (reagent/atom (initializer-state))
        values (reagent/atom {:dur nil :end nil :start nil :date nil :data nil})
        current-month (reagent/atom "JAN")
        day (reagent/atom "SUNDAY")]
    {:current-month current-month :day day :days-data days-data :values values}))
