(ns calender.container
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [calender.request :as r]
              [cljs.core.async :refer [<!]]
              [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [goog.string :as gstring]
              [goog.string.format :as format]
              [clojure.string :as str]
              [calender.component :refer [table select-input input-box submit]]
              [calender.abc :refer [conn]]))

(defn ^:private parse-int [s]
  (let [n (js/parseInt s)]
    (if-not (js/isNaN n) n)))

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


(defn is-uppercase [s]
  (= s (str/upper-case s)))

(defn update-initial-state [body state]
  (js/console.log "state initial call")
  (let [month (:month body)
        date-object  (:date body)
        curr  (keyword month)
        date  (parse-int (get-in date-object [:data :date] ))
        _ (js/console.log "date" date "month" month)
        start-times(get-in date-object [:data :startTime])
        end-times  (get-in date-object [:data :endTime  ])
        curr-date (- date 1)]
    (swap! (:days-data state)
      assoc-in
        [curr curr-date :entry :start] start-times)
   (swap! (:days-data state)
      assoc-in
        [curr curr-date :entry :end] end-times)))

;; table container
(defn handle-request [state]
  (go
    (let [data (<! (r/fetch-data))
          body (:body data)
          nodes (:nodes body)]
      (dorun (map #(update-initial-state % state) nodes)) state)))

(defn handle-click-date [id state]
  (js/console.log "id" id)
  (let [curr (keyword @(:current-month state))
        curr-date id
        curr-state (- curr-date 1)]
      (swap! (:days-data state) assoc-in [curr curr-state :entry :click] true)))

(defn table-container [state]
  (let [ current-month  @(:current-month state)
         days-data ((keyword current-month) @(:days-data state))
         month-days     (aget MONTH @(:current-month state))
         empty-first-row (get days (keyword @(:day state)))]
    [table days-data month-days empty-first-row current-month #(handle-click-date % state) #(handle-request state)]))

;; form container
(defn choose [e state]
  (reset! (:current-month state)(.-value (.-target e)))
  (let [n (aget offset @(:current-month state))
        k (->> days (group-by val)
                    (#(get % n))
                    (map key))]
  (reset! (:day state) (first k))))

(defn update-input [e id state]
  (swap! (:values state) assoc-in [(keyword id)] (-> e .-target .-value)))

;; (defn add-entry [val]
;;   (swap! enteries conj {:text (:month @val) :is-completed false})
;;   (swap! nums inc)
;;   (reset! (:month values) ""))

(defn check-nil [value]
  (let [ {:keys [data date end start dur]} value
         v (not-any? nil? [data date end start dur])] v ))

(defn save-entry [state]
  (let [value @(:values state)
        month @(:current-month state)
        body (conj value {:month month})]
        (if (check-nil value)
          (go
            (let [resp (<! (r/send-data body)) status (:status resp)]
              (when (= status 200)
                (handle-request state)))) (js/console.log "error"))))

(defn form-container [state]
  (let [value @(:values state)]
    [:div
      [input-box "date" (:date value)  #(update-input % "date" state)]
      [input-box "dur"  (:dur value)   #(update-input % "dur"  state)]
      [input-box "start"(:start value) #(update-input % "start"state)]
      [input-box "end"  (:end value)   #(update-input % "end"  state)]
      [input-box "data" (:data value)  #(update-input % "data" state)]
      [select-input #(choose % state)]
      [submit #(save-entry state)]]))



























