(ns calender.component
    (:require [calender.request :as r]
              [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [goog.string :as gstring]
              [goog.string.format :as format]
              [clojure.string :as str]))

(defn ^:private parse-int [s]
  (let [n (js/parseInt s 10)]
    (if-not (js/isNaN n)
      n)))

(def MONTH (js-obj
  "JAN" 31 "FEB" 28 "MAR" 31 "APR" 30
  "MAY" 31 "JUNE" 30 "JULY" 31 "AUG" 31
  "SEPT" 30 "OCT" 31 "NOV" 30 "DEC" 31))

(def TYPE (js-obj
  "JAN" "JAN" "FEB" "FEB" "MAR" "MAR" "APR" "APR"
  "MAY" "MAY" "JUNE" "JUNE" "JULY" "JULY" "AUG" "AUG"
  "SEPT" "SEPT" "OCT" "OCT" "NOV" "NOV" "DEC" "DEC"))

(def TITLE (js-obj
  "JAN" "JANUARY" "FEB" "FEBRUARY" "MAR" "MARCH" "APR" "APRIL"
  "MAY" "MAY" "JUNE" "JUNE" "JULY" "JULY" "AUG" "AUGUST"
  "SEPT" "SEPTEMBER" "OCT" "OCTOBER" "NOV" "NOVEMBER" "DEC" "DECEMBER"))

(def days {:SUNDAY 0 :MONDAY 1 :TUESDAY 2 :WEDNESDAY 3
           :THURSDAY 4 :FRIDAY 5 :SATURDAY 6})

(def offset (js-obj
  "JAN" 0 "FEB" 3 "MAR" 3 "APR" 6
  "MAY" 1 "JUNE" 4 "JULY" 6 "AUG" 2
  "SEPT" 5 "OCT" 0 "NOV" 3 "DEC" 5))

(defn handle-click-d [i]
  (js/console.log i))

(defn is-uppercase [s]
  (= s (str/upper-case s)))

;;----- table component -----;;

(defn calender-title [cm]
  [:h1 (aget TITLE cm)])

(defn empty-td [num]
 ^{:key (gstring/format "td_empty%s" num)}[:td])

(defn add-td [num value clicked-date]
  ^{:key (gstring/format "td%s" value) :id num}[:td {:on-click #(clicked-date num)} value ])

(defn table-first-row [total-empty non-empty handle-click]
      (let [se (concat (repeat total-empty 0) non-empty)
            k  (concat (range 0 (+ total-empty 0)) non-empty)]
            (map
              (fn [value num]
                  (if-not (= 0 value)
                    (add-td value value #(handle-click %))
                    (empty-td num))) se k)))

(defn table-middle-row [middle-row handle-click]
  (map
    (fn [value]
      (add-td value value #(handle-click %))) middle-row))

(defn table-last-row [total-empty non-empty handle-click]
    (let [se (concat non-empty (repeat total-empty 0))
          k  (concat non-empty (range (last non-empty)(+ total-empty (last non-empty))))]
      (map
        (fn [value num]
            (if-not (= 0 value)
              (add-td value value #(handle-click %))
              (empty-td num))) se k)))

(defn table
  [month-days empty-first-row curr-month handle-click-date handle-request]
  (let [ remainder      (mod (- month-days (- 7 empty-first-row)) 7)
         empty-last-row (- 7 remainder)
         days-vector    (into [] (map inc (range month-days)))
         first-row      (take (- 7 empty-first-row) days-vector)
         middle         (subvec days-vector (- 7 empty-first-row) (- month-days remainder))
         last-row       (take-last remainder days-vector)
         middle-rows    (partition 7 middle)
         coun           (count middle-rows)
         total-middle-rows (range coun)]
         (js/console.log "fr")
           [:table {:id "calender-table"}
            [:thead
             [:tr
              [:th {:id "th0" :key "th0"} "SUNDAY"]
              [:th {:id "th1" :key "th1"} "MONDAY"]
              [:th {:id "th2" :key "th2"} "TUESDAY"]
              [:th {:id "th3" :key "th3"} "WEDNESDAY"]
              [:th {:id "th4" :key "th4"} "THURSDAY"]
              [:th {:id "th5" :key "th5"} "FRIDAY"]
              [:th {:id "th6" :key "th6"} "SATURDAY"]]]
              [:tbody
                 ^{key (str 0)}[:tr (table-first-row empty-first-row first-row #(handle-click-date %))]
                  (map
                    (fn [num middle-row]
                      ^{key (str (+ 1 num))}[:tr (table-middle-row middle-row #(handle-click-date %))]) total-middle-rows middle-rows)
                  ^{key (str (+ coun 1))} [:tr (table-last-row empty-last-row last-row #(handle-click-date %))]]]))

;; form-component

(defn input-box [id value update-input]
   [:input {:on-change #(update-input %) :type "text" :id id :value value :placeholder id}])

(defn submit [save-entry]
  [:div
  [:input {:type "button" :value "Submit" :on-click  #(save-entry)}]])

(defn select-input [choose]
   [:select {:id "month-options" :on-change #(choose %)}
    [:option  {:value (.-JAN TYPE)} "jan"]
    [:option  {:value (.-FEB TYPE)} "feb"]
    [:option  {:value (.-MAR TYPE)} "mar"]
    [:option  {:value (.-APR TYPE)} "apr"]
    [:option  {:value (.-MAY TYPE)} "may"]
    [:option  {:value (.-JUNE TYPE)}"june"]
    [:option  {:value (.-JULY TYPE)}"july"]
    [:option  {:value (.-AUG TYPE)}"aug"]
    [:option  {:value (.-SEPT TYPE)} "sept"]
    [:option  {:value (.-OCT TYPE)} "oct"]
    [:option  {:value (.-NOV TYPE)} "nov"]
    [:option  {:value (.-DEC TYPE)} "dec"]])


(defn title [value]
  [:h1 value])










