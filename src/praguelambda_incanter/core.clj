(ns praguelambda-incanter.core
  (:require
    [incanter.core :refer :all :as i]
    [incanter.stats :refer :all :as stats]
    [incanter.charts :refer :all :as charts]
    [incanter.datasets :refer :all]))

; Read dataset
; -------------
; (require '[incanter.core :as i]
;          '[incanter.io :as io]
;          '[incanter.excel :as excel])
;
; (def series (io/read-dataset "./data-w-header.csv" :header true)
; (def xseries (excel/read-xls "data-w-header.xls"))

; Sample datasets
; ---------------
; http://incanter.org/docs/api/#library-incanter.datasets

(def hyc (get-dataset :hair-eye-color))
(def iris (get-dataset :iris))
(def cars (get-dataset :cars))

(comment

  ; dataframe - find out column names
  (i/col-names hyc)

  ; Column selector, $ function
  (set (i/$ :eye hyc))

  ; alias for sel
  (set (i/sel hyc :cols :eye))

  ; convert vecs/maps/.... to incanter's dataset
  (i/to-dataset [[1 2 3] [4 5 6]])

  (def small-matrix
    (i/to-matrix
      (i/to-dataset [[1 2 3] [4 5 6]])))

  ; infix notation $=
  (i/$= 3 * 4 + 8)

  (i/$= small-matrix * 2)

  (i/$= (reduce i/plus small-matrix)
        / (i/nrow small-matrix))

  ; data exploration - row selection operator
  (->> iris
       (i/$where {:Species "virginica"}))

  ; SQL-like operators
  (->> iris
       (i/$group-by :Species))

  ; Conjoin columns
  (i/view (i/conj-cols (range (i/nrow cars)) cars))

  ; Conjoin rows
  (i/view (i/conj-rows [:x :y] cars))


  ; Charting
  ; ---------

  ; basic scatter
  (i/view (charts/scatter-plot :Sepal.Length :Sepal.Width
                      :data (get-dataset :iris)))


  ; scatter colored by group
  (i/view (charts/scatter-plot :Sepal.Length :Sepal.Width
                      :data (get-dataset :iris)
                      :group-by :Species))

  ; label chart axes
  (i/view (charts/scatter-plot :Sepal.Length :Sepal.Width
                      :data (get-dataset :iris)
                      :group-by :Species
                      :title "Fisher Iris Data"
                      :x-label "Sepal Length (cm)"
                      :y-label "Sepal Width (cm)"))

  ; Sampling functions
  ;---------------------

  ; gamma distribution (1000 samples)
  (doto (charts/histogram (stats/sample-gamma 1000)
                   :density true
                   :nbins 30)
    i/view)

  ; normal distribution (1000 samples + distribution function)
  (doto (charts/histogram (stats/sample-normal 1000)
                   :density true
                   :nbins 10)
    (charts/add-function pdf-normal -4 4)
    i/view)


  ; Data exploration
  ; ----------------
  (->> iris
       ($ :Sepal.Length)
       (stats/quantile))

  ; With tagging
  (defn mysummary
    [col-vals]
    (merge (zipmap [:min :25th :50th :75th :max] (stats/quantile col-vals))
           {:count (count col-vals)
            :mean  (stats/mean col-vals)
            :sd    (stats/sd col-vals)}))

  (->> iris
       ($ :Sepal.Length)
       mysummary)


  ; Idiosyncrasies
  ;-------------------
  ; Undocumented usage of agents
  ; (clojure.core/shutdown-agents)

  (defn process-dataset
    [data]
    (let [dataset
          (->> data
               (i/to-dataset)
               (i/$where {:Species "virginica"})
               (i/$ :Petal.Length)
               )]
      (map (fn [x] ($= x * 10)) dataset)))

  (process-dataset iris)

  (process-dataset
    (zipmap (col-names iris) [1 2 3 4 "virginica"]))

  ;;;;;;;;

  (defn process-dataset
    [data]
    (let [dataset
          (->> data
               (i/to-dataset)
               (i/$where {:Species "virginica"})
               (i/$ :Petal.Length)
               )]
      (map (fn [x] ($= x * 10)) (if (seq? dataset)
                                  dataset
                                  [dataset]))))

  (process-dataset iris)

  (process-dataset
    (zipmap (col-names iris) [1 2 3 4 "virginica"]))

  (process-dataset
    (zipmap (col-names iris)
            [1 2 nil 4 "virginica"]))

  ;;;;;;;;

  (defn process-dataset
    [data]
    (let [dataset
          (->> data
               (i/to-dataset)
               (i/$where {:Species "virginica"})
               (i/$where {:Petal.Length {:$fn (complement nil?)}})
               (i/$ :Petal.Length)
               )]
      (map (fn [x] ($= x * 10)) (if (seq? dataset)
                                  dataset
                                  [dataset]))))

  (process-dataset iris)

  (process-dataset
    (zipmap (col-names iris) [1 2 3 4 "virginica"]))

  (process-dataset
    (zipmap (col-names iris)
            [1 2 nil 4 "virginica"]))

  ; Matrix: incanter's compact floating-point (double) representation
  ; needs to be used for advanced analysis functions.

  ; original dataset
  cars

  ; converted to matrix of doubles
  (i/to-matrix cars)

  )

; References
; ---------------
; Incanter blog - https://data-sorcery.org/contents/
; Data sorcery  - http://incanter.org/docs/data-sorcery-new.pdf
; Working with Incanter Datasets - https://www.packtpub.com/books/content/working-incanter-datasets
; Clojure Data Analysis Cookbook - https://www.packtpub.com/big-data-and-business-intelligence/clojure-data-analysis-cookbook
; Clojure for Data Science - https://www.packtpub.com/big-data-and-business-intelligence/clojure-data-science