(ns sqlinsql.named-parameters-test
  (:require [clojure.test :refer :all]
            [sqlinsql.named-parameters :refer :all]))

(deftest split-at-parameters-test
  (testing "Simple"
    (is (= (split-at-parameters "SELECT 1 FROM dual")
           '["SELECT 1 FROM dual"]))
    (is (= (split-at-parameters "SELECT ? FROM dual")
           '["SELECT " ? " FROM dual"]))
    (is (= (split-at-parameters "SELECT :value FROM dual")
           '["SELECT " value " FROM dual"]))
    (is (= (split-at-parameters "SELECT 'test'\nFROM dual")
           '["SELECT 'test'\nFROM dual"]))
    (is (= (split-at-parameters "SELECT :value, :other_value FROM dual")
           '["SELECT " value ", " other_value " FROM dual"])))
  (testing "Mixed parameters"
    (is (= (split-at-parameters "SELECT :value, ? FROM dual")
           '["SELECT " value ", " ? " FROM dual"])))
  (testing "Escapes"
    (is (= (split-at-parameters "SELECT :value, :other_value, ':not_a_value' FROM dual")
           '["SELECT " value ", " other_value ", ':not_a_value' FROM dual"]))
    (is (= (split-at-parameters "SELECT 'not \\' :a_value' FROM dual")
           '["SELECT 'not \\' :a_value' FROM dual"])))
  (testing "Casting"
    (is (= (split-at-parameters "SELECT :value, :other_value, 5::text FROM dual")
           '["SELECT " value ", " other_value ", 5::text FROM dual"])))
  (testing "Complex"
    (is (= (split-at-parameters "SELECT :a+2*:b+age::int FROM users WHERE username = ? AND :b > 0")
           '["SELECT " a "+2*" b "+age::int FROM users WHERE username = " ? " AND " b " > 0"]))
    (is (= (split-at-parameters "SELECT :value1 + ? + value2 + ? + :value1\nFROM SYSIBM.SYSDUMMY1")
           '["SELECT " value1 " + " ? " + value2 + " ? " + " value1 "\nFROM SYSIBM.SYSDUMMY1"]
           )))) 

(deftest convert-named-query-test
  (is (= (convert-named-query "SELECT :a+2*:b+age::int FROM users WHERE username = ? AND :b > 0")
         ["SELECT ?+2*?+age::int FROM users WHERE username = ? AND ? > 0"
          '[a b ? b]]))) 