seq_make(Car, Cdr, seq(Car,Cdr)).
seq_car(seq(Car,_), Car).
seq_cdr(seq(_,Cdr), Cdr).

seq_next(Seq0, Seq) :-
    seq_cdr(Seq0, Cdr0),
    call(Cdr0, Seq).

seq_add(SeqA, SeqB, SeqAdd) :-
    seq_car(SeqA, CarA),
    seq_car(SeqB, CarB),
    Car is CarA + CarB,
    seq_next(SeqA, SeqANext),
    seq_next(SeqB, SeqBNext),
    seq_make(Car, seq_add(SeqANext,SeqBNext), SeqAdd).


seq_take(N, Seq0, Ls0, Ls) :-
    (N =:= 0 ->
	     Ls0 = Ls
     ;	seq_car(Seq0, Car),
	    Ls0 = [Car|Rest],
	    seq_next(Seq0, Seq1),
	    N1 is N - 1,
	    seq_take(N1, Seq1, Rest, Ls)
    ).

seq_take(Seq, N, Ts) :-
    seq_take(N, Seq, Ts, []).

seq_filter(Pred, Seq0, Seq) :-
    seq_car(Seq0, Car),
    seq_next(Seq0, Seq1),
    (	call(Pred, Car) ->
	        seq_make(Car, seq_filter(Pred,Seq1), Seq)
        ;	seq_filter(Pred, Seq1, Seq)
    ).

seq_print(Seq) :-
    seq_car(Seq, Car),
    format("~w\n", [Car]),
    seq_next(Seq, Seq1),
    seq_print(Seq1).
