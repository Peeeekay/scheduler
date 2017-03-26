(ns calender.core
    (:require [reagent.core :as reagent :refer [atom]]
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
  "SEP" "SEP" "OCT" "OCT" "NOV" "NOV" "DEC" "DEC"
))

(def TITLE (js-obj
  "JAN" "JANUARY" "FEB" "FEBRUARY" "MAR" "MARCH" "APR" "APRIL"
  "MAY" "MAY" "JUNE" "JUNE" "JULY" "JULY" "AUG" "AUGUST"
  "SEP" "SEPTEMBER" "OCT" "OCTOBER" "NOV" "NOVEMBER" "DEC" "DECEMBER"
))

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
(defonce value (reagent/atom ""))
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
    [:option  {:value (.-DEC TYPE)} "dec"]
   ]
)

(defn calender-title []
  [:h1 (aget TITLE @current-month)])


(defn toggle-class [obj j]
  (let [s (:text obj)]
    (if (is-uppercase s)
      (swap! enteries assoc j {:text (str/lower-case s) :is-completed true})
      (swap! enteries assoc j {:text (str/upper-case s) :is-completed true})
      )
))

(defn clicked-date [e]
  (let [curr (keyword @current-month)
        curr-date (parse-int (.-innerHTML (.-target e)))
        curr-state (- curr-date 1)
       ]
      (js/console.log "state i" (get-in @days-data [:JAN 0 :id]))
      (swap! days-data assoc-in [curr curr-state :data] "clicked")
      (js/console.log "date" curr-date)
      (js/console.log "state" (get-in @days-data [curr]))
  ))

(defn empty-td [num]
  (.log js/console "ac")
 ^{:key (gstring/format "td%s%" num)}[:td])

(defn add-td [num,value]
   (.log js/console value)
  ^{:key (gstring/format "td%s" value)}[:td {:on-click #(clicked-date %)} value])

(defmulti table-rows (fn [empty-days non-empty-days counter last-row]
    (js/console.log "check " non-empty-days)
            (cond
              (< non-empty-days 7) :first-row
              (> last-row 0) :last-row
              (= non-empty-days 7) :middle-rows)))


(defmethod table-rows :first-row [empty-days non-empty-days counter last-row]
  (js/console.log "first-row")
    (map (fn [num]
          (if (< num empty-days)
                (empty-td num)
                (add-td num (- (+ 1 num) empty-days))))
     (range 7)))

(defmethod table-rows :middle-rows [empty-days non-empty-days counter last-row]
  (js/console.log "middle-row" counter)
  (map (fn [num,v]
          (if (< num empty-days)
                (empty-td num)
                (add-td num (+ 1 v))))
     (range 7)(range (- counter 7) counter)))

(defmethod table-rows :last-row [empty-days non-empty-days counter last-row]
  (js/console.log "last")
    (map (fn [num,v]
            (if (< num last-row)
                  (add-td num (+ 1 v))
                  (empty-td num)))
       (range 7)(range (- counter 7) counter)))

(defn table []
  (let [ month-days (aget MONTH @current-month) empty-row (get days @day) non-empty-row (- 7 empty-row) remdays (- (aget MONTH @current-month) non-empty-row)
         remainder (mod remdays 7)
         rows (if (= remainder 0)(quot remdays 7)(+ (quot remdays 7) 1))
         total-rows (+ rows 1)
         counter (reagent/atom 0)
         last-row (reagent/atom 0)
         non-empty(reagent/atom non-empty-row)]
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
            (map (fn [num]
              (when-not (= 0 num)
                (reset! non-empty 7))
              (when (> 7 (- month-days @counter))
                (reset! last-row (- month-days @counter)))
                  (.log js/console @counter)
                  ^{:key (str num)}[:tr (if (= num 0)
                               (do (reset! counter non-empty-row)(table-rows empty-row non-empty-row @counter @last-row))
                               (do (reset! counter (+ @counter 7))(table-rows 0 7 @counter @last-row)))
                  ])
                 (range total-rows))
          ]]))

(defn calender []
  [table])

(defn lister []
  (let [item
        [:div (map (fn[i,j][:div.abc {:key j :style {:color (if (:is-completed i) "green" "blue")} :on-click #(toggle-class (nth @enteries j) j)} (:text i)]) @enteries (range @nums))]] item))

(defn box []
  [:input {:on-change (fn [v](reset! value (-> v .-target .-value)) val) :type "text" :id "box" :value @value}])

(defn add-entry [val]
  (swap! enteries conj {:text @val :is-completed false})
  (swap! nums inc)
  (reset! value "")
)

(defn submit [val]
  [:div
  [:input {:type "button" :value "Submit" :on-click  #(add-entry val)}]
  [lister]])

(defn main-page []
  (reagent/create-class
    {:component-did-mount #()
     :reagent-render
       (fn[]
         [:div
           [calender-title]
           [box] [select-input]
           [submit value]
           [:br]
           [calender]
           ])
      }))

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
