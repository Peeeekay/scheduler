(ns calender.core
    (:require [calender.request :as r]
              [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [goog.string :as gstring]
              [goog.string.format :as format]
              [clojure.string :as str]))

;;initializign js object

(defn ^:private parse-int [s]
  (let [n (js/parseInt s 10)]
    (if-not (js/isNaN n)
      n)))

(def MONTH (js-obj
  "JAN" 31 "FEB" 28 "MAR" 31 "APR" 30
  "MAY" 31 "JUNE" 30 "JULY" 31 "AUG" 31
  "SEP" 30 "OCT" 31 "NOV" 30 "DEC" 31
))

(def TYPE (js-obj
  "JAN" "JAN" "FEB" "FEB" "MAR" "MAR" "APR" "APR"
  "MAY" "MAY" "JUNE" "JUNE" "JULY" "JULY" "AUG" "AUG"
  "SEPT" "SEPT" "OCT" "OCT" "NOV" "NOV" "DEC" "DEC"))

(def TITLE (js-obj
  "JAN" "JANUARY" "FEB" "FEBRUARY" "MAR" "MARCH" "APR" "APRIL"
  "MAY" "MAY" "JUNE" "JUNE" "JULY" "JULY" "AUG" "AUGUST"
  "SEP" "SEPTEMBER" "OCT" "OCTOBER" "NOV" "NOVEMBER" "DEC" "DECEMBER"))

(def days {:SUNDAY 0 :MONDAY 1 :TUESDAY 2 :WEDNESDAY 3
           :THURSDAY 4 :FRIDAY 5 :SATURDAY 6})

(def offset (js-obj
  "JAN" 0 "FEB" 3 "MAR" 3 "APR" 6
  "MAY" 1 "JUNE" 4 "JULY" 6 "AUG" 2
  "SEP" 5 "OCT" 0 "NOV" 3 "DEC" 5
))


(defn is-uppercase [s]
  (= s (str/upper-case s)))

;; -------------------------
;; Views
;; state management

(defn add-id []
    (let [ids (into []
                (map
                  (fn [i] {:data nil :id (+ i 1)})
                   (range 31)))]ids))

(defn initializer-state []
  (.log js/console "state called")
   (let [month-array '(:JAN :FEB :MAR :APR :MAY :JUNE :JULY :AUG :SEP :OCT :NOV :DEC)
         keyword-array (into [] (map keyword month-array))
         data (into {} (map
                (fn[i] {i (add-id)})
                   month-array))]
         (js/console.log month-array) data))


(defonce current-month (reagent/atom "JAN"))
(defonce values (reagent/atom {:dur nil :end nil :start nil :date nil :data nil}))
(defonce display (reagent/atom "display"))
(defonce nums (reagent/atom 0))
(defonce day (reagent/atom "SUNDAY"))
(defonce remainder (reagent/atom 0))
(defonce days-data (reagent/atom (initializer-state)))
(defonce enteries (reagent/atom []))
(defonce test-entry (reagent/atom {}))

(defn choose [e]
  (reset! current-month (.-value (.-target e)))
  (let [n (aget offset @current-month)
        k (->> days (group-by val)
                    (#(get % n))
                    (map key))]
  (reset! day (first k))
  (.log js/console @day)))


(defn select-input []
   [:select {:id "month-options" :on-change #(choose %)}
    [:option  {:value (.-JAN TYPE)} "jan"]
    [:option  {:value (.-FEB TYPE)} "feb"]
    [:option  {:value (.-MAR TYPE)} "mar"]
    [:option  {:value (.-APR TYPE)} "apr"]
    [:option  {:value (.-MAY TYPE)} "may"]
    [:option  {:value (.-JUNE TYPE)}"june"]
    [:option  {:value (.-JULY TYPE)}"july"]
    [:option  {:value (.-AUG TYPE)}"aug"]
    [:option  {:value (.-SEP TYPE)} "sept"]
    [:option  {:value (.-OCT TYPE)} "oct"]
    [:option  {:value (.-NOV TYPE)} "nov"]
    [:option  {:value (.-DEC TYPE)} "dec"]])

(defn calender-title []
  [:h1 (aget TITLE @current-month)])


(defn toggle-class [obj j]
  (let [s (:text obj)]
    (if (is-uppercase s)
      (swap! enteries assoc j {:text (str/lower-case s) :is-completed true})
      (swap! enteries assoc j {:text (str/upper-case s) :is-completed true}))))

(defn clicked-date [e]
  (let [curr (keyword @current-month)
        curr-date (parse-int (.-innerHTML (.-target e)))
        curr-state (- curr-date 1)
       ]
      (js/console.log "state i" (get-in @days-data [:JAN 0 :id]))
      (swap! days-data assoc-in [curr curr-state :data] "clicked")
      (js/console.log "date" curr-date)
      (js/console.log "state" (get-in @days-data [curr]))))

(defn empty-td [num]
 ^{:key (gstring/format "td_empty%s" num)}[:td])

(defn add-td [num,value]
  ^{:key (gstring/format "td%s" value)}[:td {:on-click #(clicked-date %)} value])

(defn table-first-row [total-empty non-empty]
  (js/console.log "first-row" (empty? non-empty))
    (when-not (= 0 total-empty)
      (let [se (concat (repeat total-empty 0) non-empty)
            k  (concat (range 0 (+ total-empty 0)) non-empty)]
            (map
              (fn [value num]
                  (if-not (= 0 value)
                    (add-td value value)
                    (empty-td num))) se k))))

(defn table-middle-row [middle-row]
  (js/console.log "middle-row")
  (map
    (fn [value]
      (add-td value value)) middle-row))

(defn table-last-row [total-empty non-empty]
  (js/console.log "last" (concat non-empty (repeat total-empty 0)))
    (let [se (concat non-empty (repeat total-empty 0))
          k  (concat non-empty (range (last non-empty)(+ total-empty (last non-empty))))]
      (map
        (fn [value num]
            (if-not (= 0 value)
              (add-td value value)
              (empty-td num))) se k)))


(defn table [month-days empty-first-row]
  (let [ remainder      (mod (- month-days (- 7 empty-first-row)) 7)
         empty-last-row (- 7 remainder)
         days-vector    (into [] (map inc (range month-days)))
         first-row      (take (- 7 empty-first-row) days-vector)
         middle         (subvec days-vector empty-first-row (- month-days remainder))
         last-row       (take-last remainder days-vector)
         middle-rows    (partition 7 middle)
         coun           (count middle-rows)
         total-middle-rows (range coun)]
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
               ^{key (str 0)}[:tr (table-first-row empty-first-row first-row)]
                (map
                  (fn [num middle-row]
                    ^{key (str (+ 1 num))}[:tr (table-middle-row middle-row)]) total-middle-rows middle-rows)
                ^{key (str (+ coun 1))} [:tr (table-last-row empty-last-row last-row)]
             ]]))


(defn table-container []
  (let [ month-days      (aget MONTH @current-month)
         empty-first-row (get days (keyword @day))]
    [table month-days empty-first-row]))


(defn calender []
  [table-container])

(defn lister []
  (let [item
        [:div (map (fn[i,j][:div.abc {:key j :style {:color (if (:is-completed i) "green" "blue")} :on-click #(toggle-class (nth @enteries j) j)} (:text i)]) @enteries (range @nums))]] item))

(defn input-box [id value]
   [:input {:on-change #(swap! values assoc-in [(keyword id)] (-> % .-target .-value)) :type "text" :id id :value value :placeholder id}])

(defn date-box  []
  (let [value (:date @values)]
    [input-box "date" value]))

(defn dur-box   []
  (let [value (:dur @values)]
  [input-box "dur" value]))

(defn end-box []
  (let [value (:end @values)]
  [input-box "end" value]))

(defn start-box []
  (let [value (:start @values)]
  [input-box "start" value]))

(defn month-box []
  (let [value (:month @values)]
        [input-box "month" value]))

(defn data-box []
  (let [value (:data @values)]
        [input-box "data" value]))

(defn add-entry [val]
  (swap! enteries conj {:text (:month @val) :is-completed false})
  (swap! nums inc)
  (reset! (:month values) ""))

(defn check-nil [value]
  (js/console.log (nil?(:dur @values)))
  (let [ {:keys [data date end start dur]} value
         v (not-any? nil? [data date end start dur])]
    (js/console.log v) v ))

(defn save-entry []
  (js/console.log "save entry called")
  (let [value @values
        month @current-month
        body (conj value {:month month})]
        (if (check-nil value)(r/send-data body)(js/console.log "error"))))

(defn submit []
  [:div
  [:input {:type "button" :value "Submit" :on-click  #(save-entry)}]
  [lister]])

(defn main-page []
  (reagent/create-class
    {:component-did-mount #()
     :reagent-render
       (fn[]
         [:div
           [calender-title]
           [date-box][data-box][select-input][dur-box][start-box][end-box]
           [submit]
           [:br]
           [calender]])}))

(defn home-page []
  [:div [:h2 "Welcome to calender"]
   [:div [:a {:href "/about"} "go to about page"]]
   [:div [:a {:href "/main"} "go to my calender app"]]])

(defn about-page []
  [:div [:h2 "About calender"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/main" []
  (session/put! :current-page #'main-page))


;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
