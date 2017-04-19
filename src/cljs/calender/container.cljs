(ns calender.container
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [goog.string :as gstring]
              [goog.string.format :as format]
              [clojure.string :as str]
              [calender.state :as state :refer [state]]))

(defn ^:private parse-int [s]
  (let [n (js/parseInt s 10)]
    (if-not (js/isNaN n)
      n)))

(def MONTH (js-obj
  "JAN" 31 "FEB" 28 "MAR" 31 "APR" 30
  "MAY" 31 "JUNE" 30 "JULY" 31 "AUG" 31
  "SEP" 30 "OCT" 31 "NOV" 30 "DEC" 31))

(def TYPE (js-obj
  "JAN" "JAN" "FEB" "FEB" "MAR" "MAR" "APR" "APR"
  "MAY" "MAY" "JUNE" "JUNE" "JULY" "JULY" "AUG" "AUG"
  "SEP" "SEP" "OCT" "OCT" "NOV" "NOV" "DEC" "DEC"))

(def TITLE (js-obj
  "JAN" "JANUARY" "FEB" "FEBRUARY" "MAR" "MARCH" "APR" "APRIL"
  "MAY" "MAY" "JUNE" "JUNE" "JULY" "JULY" "AUG" "AUGUST"
  "SEP" "SEPTEMBER" "OCT" "OCTOBER" "NOV" "NOVEMBER" "DEC" "DECEMBER"))

(def days {:SUNDAY 0 :MONDAY 1 :TUESDAY 2 :WEDNESDAY 3
           :THURSDAY 4 :FRIDAY 5 :SATURDAY 6})

(def offset (js-obj
  "JAN" 0 "FEB" 3 "MAR" 3 "APR" 6
  "MAY" 1 "JUNE" 4 "JULY" 6 "AUG" 2
  "SEP" 5 "OCT" 0 "NOV" 3 "DEC" 5))

(defn table-container []
  (js/console.log (get days (keyword@(:day (state)))))
  (let [ curr-state (state)
         day (:day curr-state)
         current-month (:current-month curr-state)
         _ (js/console.log day)]
;;          update-non-empty #(do (reset! day "SUNDAY") (js/console.log @day))
;;          update-last-row #(reset! last-row (- % @counter))
;;          update-counter #(reset! counter %)]
         (js/console.log "about to call" @day)))
;;   [table @day @current-month @counter @last-row @non-empty empty-row non-empty-row update-counter update-last-row update-non-empty]))









