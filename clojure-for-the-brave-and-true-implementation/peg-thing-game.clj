(ns pegthing.core
  (require [clojure.set :as set])
  (:gen-class))

;{1 {:pegged true, :connections {6 3, 4 2}},
;2 {:pegged true, :connections {9 5, 7 4}},
;3 {:pegged true, :connections {10 6, 8 5}},
;4 {:pegged true, :connections {13 8, 11 7, 6 5, 1 2}},
;5 {:pegged true, :connections {14 9, 12 8}},
;6 {:pegged true, :connections {15 10, 13 9, 4 5, 1 3}},
;7 {:pegged true, :connections {9 8, 2 4}},
;8 {:pegged true, :connections {10 9, 3 5}},
;9 {:pegged true, :connections {7 8, 2 5}},
;10 {:pegged true, :connections {8 9, 3 6}},
;11 {:pegged true, :connections {13 12, 4 7}},
;12 {:pegged true, :connections {14 13, 5 8}},
;13 {:pegged true, :connections {15 14, 11 12, 6 9, 4 8}},
;14 {:pegged true, :connections {12 13, 5 9}},
;15 {:pegged true, :connections {13 14, 6 10}},
;5 :rows}

;{:pegged true, :connections {6 3, 4 2}}

;The meaning of :pegged is clear; it represents whether that position has
;a peg in it. :connections is a bit more cryptic. It’s a map where each key
;identifies a legal destination, and each value represents the position that
;would be jumped over. So pegs in position 1, for example, can jump to
;position 6 over position 3.

;The first few expressions in this section of the code deal with triangular
;numbers. Triangular numbers are generated by adding the first n natural
;numbers. The first triangular number is 1, the second is 3 (1 + 2), the third
;is 6 (1 + 2 + 3), and so on. These numbers line up nicely with the position
;numbers at the end of every row on the board, which will turn out to be a very useful property

(defn tri*
  "Generates lazy sequence of triangular numbers"
  ([] (tri* 0 1))
  ([sum n]
      (let [new-sum (+ sum n)]
        (cons new-sum (lazy-seq (tri* new-sum (inc n)))))))

;The next expression calls tri*, actually creating the lazy sequence and
;binding it to tri
(def tri (tri*))

;(take 5 tri)

(defn triangular?
	"Is the number triangular? e.g 1, 3,6,10 ,15, etc"
	[n]
	(= n (last (take-while #(>= n %) tri))))

;(triangular? 5)
;(take-while #(>= 6 %) tri)

(defn row-tri
  "The triangular number at the end of row n "
  [n]
  (last (take n tri)))


(defn row-num
  "Returns row number the position belongs to: pos 1 in row 1
  positions 2 and 3 in row 2, etc"
  [pos]
  (inc (count (take-while #(> pos %) tri))))

(defn connect
  "From a mutual connection between two positions"
  [board max-pos pos neighbor destination]
  (if (<= destination max-pos)
    (reduce (fn [new-board [p1 p2]]
                    (assoc-in new-board [p1 :connections p2] neighbor))
                board
                [[pos destination] [destination pos]])
    board))

(connect {} 15 1 2 4)

;=>{1 {:connections {4 2}},
;4 {:connections {1 2}}}

;exempla assoc-in
(assoc-in {} [:cookie :monster :vocals] "Finntroll")
;=>{:cookie {:monster {:vocals "Finntroll"}}}

(get-in {:cookie {:monster {:vocals "Finntroll"}}} [:cookie :monster])
; =>{:vocals "Finntroll"}

(assoc-in {} [1 :connections 4] 2)
; => {1 {:connections {4 2}}}


;In these examples, you can see that new, nested maps are created to
;accommodate all the keys provided.
;Now we have a way to connect two positions, but how should the
;program choose two positions to connect in the first place? That’s handled
;by connect-right, connect-down-left, and connect-down-right:

(defn connect-right
  [board max-pos pos]
  (let [neighbor (inc pos)
        destination (inc neighbor)]
    (if-not (or (triangular? neighbor) (triangular? pos))
      (connect board max-pos pos neighbor destination)
      board)))

(defn connect-down-left
  [board max-pos pos]
  (let [row (row-num pos)
        neighbor (+ row pos)
        destination (+ 1 row neighbor)]
    (connect board max-pos pos neighbor destination)))

(defn connect-down-right
  [board max-pos pos]
  (let [row (row-num pos)
        neighbor (+ 1 row pos)
        destination (+ 2 row neighbor)]
    (connect board max-pos pos neighbor destination)))

;These functions each take the board’s max position and a board position
;and use a little triangle math to figure out which numbers to feed to
;connect. For example, connect-down-left will attempt to connect
;position 1 to position 4. In case you’re wondering why the functions
;connect-left, connect-up-left, and connect-up-right aren’t
;defined, the reason is that the existing functions actually cover these cases.
;connect returns a board with the mutual connection established; when 4
;connects right to 6, 6 connects left to 4. Here are a couple of examples:


(connect-down-left {} 15 1)
; => {1 {:connections {4 2} 4 {:connections {1 2}}}}

(connect-down-right {} 15 3)

; => {3 {:connections {10 6}} 10 {:connections {3 6}}}

;The next function, add-pos, is interesting because it actually reduces on
;a vector of functions, applying each in turn to build up the resulting board.
;But first it updates the board to indicate that a peg is in the given position:


(defn add-pos
  "Pegs the position and performs connections"
  [board max-pos pos]
  (let [pegged-board (assoc-in board [pos :pegged] true)]
    (reduce (fn [new-board connection-creation-fn]
               (connection-creation-fn new-board max-pos pos pos))
            pegged-board
            [connect-right connect-down-left connect-down-right])))

(add-pos {} 15 1)
;=>{1 {:connections {6 3, 4 2}, :pegged true}
;=>4 {:connections {1 2}}
;=>6 {:connections {1 3}}}

(defn new-board
  "Creates a new board with the given number of rows"
  [rows]
  (let [initial-board {:rows rows}
        max-pos (row-tri rows)]
    (reduce (fn [board pos] (add-pos board max-pos pos))
            initial-board
            (range 1 (inc max-pos)))))



;Moving pegs
(defn pegged?
  "Does the position have a peg in it?"
  [board pos]
  (get-in board [pos :pegged]))

(defn place-peg
"Put a peg in the board at given position"
[board pos]
(assoc-in board [pos :pegged] true))

(defn remove-peg
  "Take the peg at given position out of the board"
  [board pos]
  (assoc-in board [pos :pegged] false))

(defn move-peg
  "Take peg out of p1 and place it in p2"
  [board p1 p2]
  (place-peg (remove-peg board p1) p2))


(defn valid-moves
  "Return a map of all valid moves for pos, where the key is
  the destination and the value is the jumped position"
  [board pos]
  (into {}
        (filter (fn [[destination jumped]]
                  (and not ((pegged? board destination))
                       (pegged? board jumped)))
                (get-in board [pos :connections]))))

(def my-board (assoc-in (new-board 5) [4 :pegged] false))

(valid-moves my-board 1) ; => {4 2}
(valid-moves my-board 6) ; => {4 5}
(valid-moves my-board 11) ; => {4 7}
(valid-moves my-board 5) ; => {}
(valid-moves my-board 8) ; => {}

;You might be wondering why valid-moves returns a map instead of,
;say, a set or vector. The reason is that returning a map allows you to easily
;look up a destination position to check whether a specific move is valid,
;which is what valid-move? (the next function) does:

(defn valid-move?
  "Return jumped position if the move from p1 to p2 is valid, nil otherwise"
  [board p1 p2]
  (get (valid-moves board p1) p2))

(valid-move? my-board 8 4) ; => nil
(valid-move? my-board 1 4) ; => 2

;Notice that valid-move? looks up the destination position from the map
;and then returns the position of the peg that would be jumped over. This is
;another nice benefit of having valid-moves return a map, because the
;jumped position retrieved from the map is exactly what we want to pass on
;to the next function, make-move. When you take the time to construct a rich
;data structure, it’s easier to perform useful operations

(defn make-move
  "Move peg from p1 to p2, removing jumped peg"
  [board p1 p2]
  (if-let [jumped (valid-move? board p1 p2)]
    (move-peg (remove-peg board jumped) p1 p2)))

;Finally, the function can-move? is used to determine whether the game
;is over by finding the first pegged positions with moves available:

(defn can-move?
  "Do any of the pegged positions have valid moves?"
  [board]
  (some (comp not-empty (partial valid-moves board))
        (map first (filter #
                           (get (second %) :pegged) board))))

;([1 {:connections {6 3, 4 2}, :pegged true}]
;[2 {:connections {9 5, 7 4}, :pegged true}])

;The first element of the tuple is a position number, and the second is that
;position’s information. filter then applies the anonymous function #(get
;(second %) :pegged) to each of these tuples, filtering out the tuples
;where the position’s information indicates that the position is not currently
;housing a peg. Finally, the result is passed to map, which calls first on
;each tuple to grab just the position number from the tuples.
;After you get a seq of pegged positions numbers, you call a predicate
;function on each one to find the first position that returns a truthy value. The
;predicate function is created with (comp not-empty (partial validmoves board)). The idea is to first return a map of all valid moves for a
;position and then test whether that map is empty.
;First, the expression (partial valid-moves board) derives an
;anonymous function from valid-moves with the first argument, board,
;filled in using partial (because you’re using the same board each time you
;call valid-moves). The new function can take a position and return the
;map of all its valid moves for the current board.
;Second, you use comp to compose this function with not-empty. This
;function is self-descriptive; it returns true if the given collection is empty
;and false otherwise.
;What’s most interesting about this bit of code is that you’re using a chain
;of functions to derive a new function, similar to how you use chains of
;functions to derive new data. In Chapter 3, you learned that Clojure treats
;functions as data in that functions can receive functions as arguments and
;return them. Hopefully, this shows why that feature is fun and useful.


; Rendering and printing the board

(def alpha-start 97)
(def alpha-end 123)
(def letters (map (comp str char) (range alpha-start alphaend)))
(def pos-chars 3)

;the bindings alpha-start and alpha-end set up the beginning and
;end of the numerical values for the letters a through z. We use those to build
;up a seq of letters. char, when applied to an integer, returns the character
;corresponding to that integer, and str turns the char into a string. 
;poschars is used by the function row-padding to determine how much
;spacing to add to the beginning of each row. The next few definitions,
;ansi-styles, ansi, and colorize output colored text to the terminal.
;The functions render-pos, row-positions, row-padding, and
;render-row create strings to represent the board


(defn render-pos
  [board pos]
  str ((nth letters (dec pos))
       (if (get-in board [pos :pegged])
         (colorize "0" :blue)
         (colorize "-" :red))))


(defn row-positions
  "Return all positions in the given row"
  [row-num]
  (range (inc (or (row-tri (dec row-num)) 0))
         (inc (row-tri row-num))))


(defn row-padding
  "String of spaces to add to the beginning of a row to cen ter it"
  [row-num rows]
  (let [pad-length (/ (* (- rows row-num) pos-chars) 2)]
    (apply str (take pad-length (repeat " ")))))

(defn render-row
  [board row-num]
  (str (row-padding row-num (:rows board))
       (clojure.string/join " " (map (partial renderpos board)
                                     (row-positions rownum)))))


(defn print-board
  [board]
  (doseq [row-num (range 1 (inc (:rows board)))]
    (println (render-row board row-num))))



;Player Interaction

;The next collection of functions handles player interaction. First, there’s
;letter->pos, which converts a letter (which is how the positions are
;displayed and identified by players) to the corresponding position number:

(defn letter->pos
  "Converts a letter string to the corresponding position number"
  [letter]
  (inc (- (int (first letter)) alpha-start)))


(declare successful-move prompt-move game-over query-rows)





(defn get-input
  "Waits for user to enter text and hit enter, then cleans the input"
  ([] (get-input nil))
  ([default]
   (let [input (clojure.string/trim (read-line))]
     (if (empty? input)
       default
       (clojure.string/lower-case input)))))

;The next function, characters-as-strings, is a tiny helper function
;used by prompt-move to take in a string and return a collection of letters
;with all nonalphabetic input discarded:


(characters-as-strings "a b")
; => ("a" "b")

(characters-as-strings "a cb")
; => ("a" "c" "b")

(defn prompt-move
  [board]
  (println "\nHere's your board:")
  (print-board board)
  (println "Move from where to where? Enter two letters:")
  (let [input (map letter->pos (characters-as-strings (getinput)))]
    (if-let [new-board (makemove➊ board (first input) (second input))]
      (user-entered-valid-move new-board)
      (user-entered-invalid-move board))))

;However, if the move is valid, the new-board is passed off to userentered-valid-move, which hands control back to prompt-move if there
;are still moves to be made:

(defn user-entered-invalid-move
  "Handles the next step after a user has entered an invalid move"
  [board]
  (println "\n!!! That was an invalid move :(\n")
  (prompt-move board))


(defn game-over
  "Announce the game is over and prompt to play again"
  [board]
  (let [remainingpegs (count (filter :pegged (vals board)))]
    (println "Game over! You had" remainingpegs "pegs left:")
    (print-board board)
    (println "Play again? y/n [y]")
    (let [input (get-input "y")]
      (if (= "y" input)
        (prompt-rows)
        (do (println "Bye!")
            (System/exit 0))))))


(defn prompt-empty-peg
  [board]
  (println "Here's your board:")
  (print-board board)
  (println "Remove which peg? [e]")
  (prompt-move (remove-peg board (letter->pos (getinput "e")))))


(defn prompt-rows
  []
  (println "How many rows? [5]")
  (let [rows (Integer. (get-input 5))
        board (new-board rows)]
    (prompt-empty-peg board)))
