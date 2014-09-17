zipper(Position, List, Zip, Element) :-
    zipper(Position, List, [], Zip, Element).
zipper(1, [Head|Tail], Acc, zip(Acc,Head,Tail), Head).
zipper(N, [Head|Tail], Acc, zip(Before,Element,After), Element) :-
    N > 1,
    M is N - 1,
    zipper(M, Tail, [Head|Acc], zip(Before,Element,After), Element).

next(zip(Before,Element,[Head|Tail]), zip([Element|Before],Head,Tail)).
previous(X, Y) :- next(Y, X).

top(Zip, Zip) :-
    Zip = zip([], _, _).
top(Zip, TZip) :-
    previous(Zip, PZip),
    top(PZip, TZip).

unzip(Zip, [Head|Tail]) :-
    top(Zip, TZip),
    TZip = zip(_, Head, Tail).

find(Zip, Match, Zip) :-
    Zip = zip(_,Match,_).
find(Zip, Match, Res) :-
    next(Zip, Next),
    find(Next, Match, Res).

replace1(Zip, Match, Replace, RZip) :-
    Zip = zip(Before,Match,After),
    RZip = zip(Before,Replace,After).
replace1(Zip, Match, _, RZip) :-
    Zip = zip(Before,NoMatch,After),
    Match \= NoMatch,
    RZip = zip(Before,NoMatch,After).

replace(Zip, Match, Replace, RZip) :-
    Zip = zip(_, _, []),
    replace1(Zip, Match, Replace, RZip).
replace(Zip, Match, Replace, Res) :-
    replace1(Zip, Match, Replace, RZip),
    next(RZip, NZip),
    replace(NZip, Match, Replace, Res).

list_replace(L1, Match, Replace, L2) :-
    zipper(1, L1, Zip, _),
    replace(Zip, Match, Replace, RZip),
    unzip(RZip, L2).
