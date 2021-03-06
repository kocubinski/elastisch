(ns clojurewerkz.elastisch.rest-api.get-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.rest.index         :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))


(use-fixtures :each fx/reset-indexes)

(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")


;;
;; get
;;

(deftest test-get-with-non-existing-document
  (is (nil? (doc/get index-name index-type "1"))))


;;
;; present?
;;

(deftest test-present-on-non-existing-id
  (is (not (doc/present? index-name index-type "1"))))

(deftest test-present-on-existing-id
  (doc/put index-name index-type "1" fx/person-jack)
  (is (doc/present? index-name index-type "1")))


;;
;; count
;;

(deftest test-count-with-a-term-query
  (idx/create index-name :mappings fx/people-mapping)
  (doc/create index-name index-type fx/person-jack)
  (doc/create index-name index-type fx/person-joe)
  (idx/refresh index-name)
  (are [c r] (is (= c (count-from r)))
       2 (doc/count index-name index-type)))

(deftest test-count-with-a-term-query
  (idx/create index-name :mappings fx/people-mapping)
  (doc/create index-name index-type fx/person-jack)
  (doc/create index-name index-type fx/person-joe)
  (idx/refresh index-name)
  (are [c r] (is (= c (count-from r)))
       1 (doc/count index-name index-type (q/term :username "esjack"))
       1 (doc/count index-name index-type (q/term :username "esjoe"))
       0 (doc/count index-name index-type (q/term :username "esmary"))))



;;
;; mget
;;

(deftest multi-get-test
  (doc/put index-name index-type "1" fx/person-jack)
  (doc/put index-name index-type "2" fx/person-mary)
  (doc/put index-name index-type "3" fx/person-joe)
  (let [mget-result (doc/multi-get
                     [{:_index index-name :_type index-type :_id "1"}
                      {:_index index-name :_type index-type :_id "2"}])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result)))))
  (let [mget-result (doc/multi-get index-name
                                   [{:_type index-type :_id "1"}
                                    {:_type index-type :_id "2"}])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result)))))
  (let [mget-result (doc/multi-get index-name index-type
                                   [{:_id "1"} {:_id "2"}])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result))))))
