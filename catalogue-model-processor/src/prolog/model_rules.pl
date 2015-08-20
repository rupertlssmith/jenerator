/* These define the ranges of the various Java integer types. */
long_max(X) :- X is 2 ** 63 - 1.
long_min(X) :- X is -2 ** 63.

int_max(X) :- X is 2 ** 31 - 1.
int_min(X) :- X is -2 ** 31.

short_max(X) :- X is 2 ** 15 - 1.
short_min(X) :- X is -2 ** 15.

byte_max(X) :- X is 2 ** 7 - 1.
byte_min(X) :- X is -2 ** 7.

java_byte(X) :- integer(X), byte_max(Max), byte_min(Min), X >= Min, X =< Max.
java_short(X) :- integer(X), short_max(Max), short_min(Min), X >= Min, X =< Max.
java_int(X) :- integer(X), int_max(Max), int_min(Min), X >= Min, X =< Max.
java_long(X) :- integer(X), long_max(Max), long_min(Min), X >= Min, X =< Max.
java_float(X) :- float(X).
java_double(X) :- float(X).

/* ======== normal_type/4
   Type normalization rules. Some of the type definitions have implicit defaults that need to be expanded.
   Product types require some restructuring to extract the fields from unique blocks and to move implicit type
   definitions in collections to the top level.

   This type expansion also deduces what the basic type will be. The Java/Hibernate translation phase turns
   that into concrete assignment to Java/Hibernate types. Other translations can assign types appropriately.
*/

normal_type(decimal, X, Prec, P) :- normal_type_decimal(decimal, X, Prec, P).
normal_type(integer_range, X, int, P) :- normal_type_int(integer_range, X, int, P).
normal_type(real_range, X, Prec, P) :- normal_type_real(real_range, X, Prec, P).
normal_type(string_pattern, X, string, P) :- normal_type_string(string_pattern, X, string, P).
normal_type(date_range, X, date, P) :- normal_type_date(date_range, X, date, P).
normal_type(time_range, X, time, P) :- normal_type_time(time_range, X, time, P).
normal_type(enumeration_type, X, enumeration, P) :- normal_type_enumeration(enumeration_type, X, enumeration, P).
normal_type(hierarchy_type, X, hierarchy, P) :- normal_type_hierarchy(hierarchy_type, X, hierarchy, P).
normal_type(view_type, X, class, P) :- normal_type_product(view_type, X, class, P).
normal_type(component_type, X, class, P) :- normal_type_product(component_type, X, class, P).
normal_type(entity_type, X, class, P) :- normal_type_product(entity_type, X, class, P).
normal_type(dimension_type, X, class, P) :- normal_type_product(dimension_type, X, class, P).
normal_type(fact_type, X, class, P) :- normal_type_product(fact_type, X, class, P).

/* ======== normal_type_int/4 */

/* These rules expand integer ranges with only a from or a to specified to use the narrowest Java type
   available. */
normal_type_int(integer_range, X, int, [from(From), to(IM)]) :- 
    type_instance(X, integer_range, [from(From)]),java_int(From),int_max(IM).

normal_type_int(integer_range, X, long, [from(From), to(LM)]) :- 
    type_instance(X, integer_range, [from(From)]),java_long(From),long_max(LM),not(java_int(From)).

normal_type_int(integer_range, X, int, [from(IM), to(To)]) :- 
    type_instance(X, integer_range, [to(To)]),java_int(To), int_min(IM).

normal_type_int(integer_range, X, long, [from(LM), to(To)]) :- 
    type_instance(X, integer_range, [to(To)]),java_long(To), long_min(LM),not(java_int(To)).

/* These rules expand integer ranges to use either a java int or long depending on the size of the from and
   to. */
normal_type_int(integer_range, X, int, [from(From), to(To)]) :-
    type_instance(X, integer_range, Params),
    member(from(From),Params),
    member(to(To),Params),
    java_int(From),
    java_int(To).

normal_type_int(integer_range, X, long, [from(From), to(To)]) :-
    type_instance(X, integer_range, Params),
    member(from(From),Params),
    member(to(To),Params),
    java_long(From),
    java_long(To),
    not((java_int(From),java_int(To))).

/* ======== normal_type_decimal/4 */

/* These rules expand decimal ranges with optional from and to, to appropriate limits depending on the
   precision. */
normal_type_decimal(decimal, X, bigdecimal, [precision(Precision), scale(Scale), from(From), to(To)]) :-
    type_instance(X, decimal_type, Params),
    member(precision(Precision), Params),
    member(scale(Scale), Params),
    member(from(From), Params),
    member(to(To), Params),
    java_int(Precision),
    java_int(Scale).

normal_type_decimal(decimal, X, bigdecimal, [precision(Precision), scale(Scale), from("unbounded"), to(To)]) :-
    type_instance(X, decimal_type, Params),
    member(precision(Precision), Params),
    member(scale(Scale), Params),
    not(member(from(_), Params)),
    member(to(To), Params),
    java_int(Precision),
    java_int(Scale).

normal_type_decimal(decimal, X, bigdecimal, [precision(Precision), scale(Scale), from(From), to("unbounded")]) :-
    type_instance(X, decimal_type, Params),
    member(precision(Precision), Params),
    member(scale(Scale), Params),
    member(from(From), Params),
    not(member(to(_), Params)),
    java_int(Precision),
    java_int(Scale).

normal_type_decimal(decimal, X, bigdecimal, [precision(Precision), scale(Scale), from("unbounded"), to("unbounded")]) :-
    type_instance(X, decimal_type, Params),
    member(precision(Precision), Params),
    member(scale(Scale), Params),
    not(member(from(_), Params)),
    not(member(to(_), Params)),
    java_int(Precision),
    java_int(Scale).

/* ======== normal_type_real/4 */

/* These rules expand real ranges with only a from or a to specified to appropriate limits depending on the
   precision. */
normal_type_real(real_range, X, float, [from(From)]) :-
    type_instance(X, real_range, Params),
    member(from(From), Params),
    not(member(to(_), Params)),
    not(member(precision(double), Params)),
    java_float(From).

normal_type_real(real_range, X, double, [from(From)]) :-
    type_instance(X, real_range, Params),
    member(from(From), Params),
    not(member(to(_), Params)),
    member(precision(double), Params),
    java_double(From).

normal_type_real(real_range, X, float, [to(To)]) :-
    type_instance(X, real_range, Params),
    member(to(To), Params),
    not(member(from(_), Params)),
    not(member(precision(double), Params)),
    java_float(To).

normal_type_real(real_range, X, double, [to(To)]) :-
    type_instance(X, real_range, Params),
    member(to(To), Params),
    not(member(from(_), Params)),
    member(precision(double), Params),
    java_double(To).

/* These rules expand real ranges to use either a java float or double depending on the precision. */
normal_type_real(real_range, X, float, [from(From), to(To)]) :-
    type_instance(X, real_range, Params),
    member(from(From), Params),
    member(to(To), Params),
    not(member(precision(double), Params)),
    java_float(From),
    java_float(To).

normal_type_real(real_range, X, double, [from(From), to(To)]) :-
    type_instance(X, real_range, Params),
    member(from(From), Params),
    member(to(To), Params),
    member(precision(double), Params),
    java_double(From),
    java_double(To).

/* ======== normal_type_string/4
   Strings with no length or pattern specified default to unlimited length, or no pattern.
*/
normal_type_string(string_pattern, X, string, [length(unlimited)|Params]) :-
    type_instance(X, string_pattern, Params),
    member(regexp(_), Params),
    not(member(length(_), Params)).

normal_type_string(string_pattern, X, string, [regexp(none)|Params]) :-
    type_instance(X, string_pattern, Params),
    member(length(_), Params),
    not(member(regexp(_), Params)).

normal_type_string(string_pattern, X, string, [length(unlimited), regexp(none)|Params]) :-
    type_instance(X, string_pattern, Params),
    not(member(regexp(_), Params)),
    not(member(length(_), Params)).

/* ======== normal_type_date/4
   Date ranges are expended to ensure that the from and to parameters fields always 
   exist, even if they are set to null. It is marked as having date type.
*/
normal_type_date(date_range, X, date, [from(From), to(To)]) :-
    type_instance(X, date_range, Params),
    member(from(From),Params),
    member(to(To),Params).

normal_type_date(date_range, X, date, [from(From), to(null)]) :-
    type_instance(X, date_range, Params),
    member(from(From),Params),
    not(member(to(To),Params)).

normal_type_date(date_range, X, date, [from(null), to(To)]) :-
    type_instance(X, date_range, Params),
    not(member(from(From),Params)),
    member(to(To),Params).

/* ======== normal_type_time/4 */

/* Time ranges without a from or to get midnight as a default. */
normal_type_time(time_range, X, time, [from(MN), to(MN), step(null)]) :-
    type_instance(X, time_range, Params),
    not(member(from(_),Params)),
    not(member(to(_),Params)),
    not(member(step(_),Params)),
    MN = "00:00:00".

normal_type_time(time_range, X, time, [from(From), to(MN), step(null)]) :-
    type_instance(X, time_range, Params),
    member(from(From),Params),
    not(member(to(_),Params)),
    not(member(step(_),Params)),
    MN = "00:00:00".

normal_type_time(time_range, X, time, [from(MN), to(To), step(null)]) :-
    type_instance(X, time_range, Params),
    not(member(from(_),Params)),
    member(to(To),Params),
    not(member(step(_),Params)),
    MN = "00:00:00".

normal_type_time(time_range, X, time, [from(From), to(To), step(null)]) :-
    type_instance(X, time_range, Params),
    member(from(From),Params),
    member(to(To),Params),
    not(member(step(_),Params)).

normal_type_time(time_range, X, time, [from(MN), to(MN), step(Step)]) :-
    type_instance(X, time_range, Params),
    member(step(Step),Params),
    not(member(from(_),Params)),
    not(member(to(_),Params)),
    MN = "00:00:00".

normal_type_time(time_range, X, time, [from(From), to(MN), step(Step)]) :-
    type_instance(X, time_range, Params),
    member(from(From),Params),
    not(member(to(_),Params)),
    member(step(Step),Params),
    MN = "00:00:00".

normal_type_time(time_range, X, time, [from(MN), to(To), step(Step)]) :-
    type_instance(X, time_range, Params),
    not(member(from(_),Params)),
    member(to(To),Params),
    member(step(Step),Params),
    MN = "00:00:00".

normal_type_time(time_range, X, time, [from(From), to(To), step(Step)]) :-
    type_instance(X, time_range, Params),
    member(from(From),Params),
    member(to(To),Params),
    member(step(Step),Params).

/* ======== normal_type_enumeration/4 */
normal_type_enumeration(enumeration_type, X, enumeration, Params) :-
    type_instance(X, enumeration_type, Params).

/* ======== normal_type_hierarchy/4 */
normal_type_hierarchy(hierarchy_type, X, hierarchy, Params) :-
    type_instance(X, hierarchy_type, Params).

/* ======== normal_type_product/4
Component types require normalization. 

Fields wrapped in a unique block are pushed up a level. This procedure makes a unique field a top-level field
of the component it appears in, and adds a unique constraint for it to the component. This pushing up procedure
is also applied to unique blocks within collections or extensions. Makes use of the unique_type_param_accum/2
predicate. 

Collections within a product type may specify components that need to be modelled as classes. This occurs when
a collection contains more than one field.

If a collection contains only one field and that field is a basic value type then a component does not need to
be created for it. If a collection contains only one field and that field is a reference to another component
then that is modelled as either an aggregation of components belonging to the parent or a full parent/child
entity relationship, depending on whether the reference is to a component or an entity. In that case a seperate
component does not need to be created for the relationship; the parent will simply hold a collection of
references to the component.

If a collection contains multiple fields then a seperate top level component is created for it and the parent
will contain a collection that refers to that component. In this way a collection containing multiple fields is
transformed into one containing just one field; a reference to the newly created top level component.

The first clause of the normal_type_product/4 predicate takes a named type instance from the model, and finds a 
normalized type for it, that contains references to any of its collections that were promoted to new top-level
types. Uses the collection_type_param_accum/3 predicate to do this.

The second clause takes a named type instance from the model, and finds any new top-level types that must exist
as normalized types, because they appear as multi-field collections within the named type. The
promoted_collection_accum/3 predicate is used to find these.
*/
normal_type_product(CompType, Name, class, Accum) :-
    product_type(CompType),
    type_instance(Name, CompType, Params),
    unique_type_param_accum(UniqueAccum, Params),
    collection_type_param_accum(Name, Accum, UniqueAccum).

normal_type_product(CompType, CollName, class, CollComponentParams) :-
    product_type(CompType),
    type_instance(Name, CompType, Params),
    promoted_collection_accum(Name, Components, Params),
    member(top_level_component(CollName, CollComponentParams), Components).

/* ======== unqiue_type_param_accum/2
   Accumulates the unique fields and constraints by the unique_field_accum/3 predicate accross all
   parameters of a product type.
*/
unique_type_param_accum([], []).

unique_type_param_accum([Prop|AccumProperties],[Prop|Properties]) :- 
    Prop \= fields(_),
    unique_type_param_accum(AccumProperties, Properties).

unique_type_param_accum(Result,[fields(Fields)|Properties]) :- 
    unique_field_accum(UniqueConstraints, AccumFields, Fields),
    TempResult = [fields(AccumFields)|UniqueConstraints],
    append(TempResult, AccumProperties, Result),
    unique_type_param_accum(AccumProperties, Properties).

/* ======== unique_field_accum/3
   Accumulates all the fields from a list of fields such that any that are included in a unique functor get
   promoted to the top level of the list and a new functor giving the names of the unique fields is also
   appended onto the start of the list for each block of unique fields.
   
   Unique blocks within the properties of collection or extension functors also get promoted up to the
   property level of the collection functor and the list of unique field names is appended onto the start
   of the list of properties for the collection.
*/
unique_field_accum([], [], []).

unique_field_accum(UniqueConstraints, [Field|AccumFields], [Field|Fields]) :-
    Field = property(_, _, _),
    unique_field_accum(UniqueConstraints, AccumFields, Fields).

unique_field_accum(UniqueConstraints, [Field|AccumFields], [Field|Fields]) :-
    Field = component_ref(_, _, _, _),
    unique_field_accum(UniqueConstraints, AccumFields, Fields).

unique_field_accum(UniqueConstraints, Result, [Field|Fields]) :-
    Field = unique(Key, fields(ConstrainedFields)),
    property_names_accum(Names, ConstrainedFields),
    append(ConstrainedFields, AccumFields, Result),
    append([unique_fields(Key, Names)], AccumUniqueConstraints, UniqueConstraints),
    unique_field_accum(AccumUniqueConstraints, AccumFields, Fields).

unique_field_accum(UniqueConstraints, 
                   [collection(CollKind, CollName, Parent, fields(UniqueCollFields))|AccumFields], 
                   [Field|Fields]) :-
    Field = collection(CollKind, CollName, Parent, fields(CollFields)),
    unique_field_accum(_, UniqueCollFields, CollFields),
    unique_field_accum(UniqueConstraints, AccumFields, Fields).

unique_field_accum(UniqueConstraints, [extend(ExtRef, UniqueExtFields)|AccumFields], [Field|Fields]) :-
    Field = extend(ExtRef, fields(ExtFields)),
    unique_field_accum(_, UniqueExtFields, ExtFields),
    unique_field_accum(UniqueConstraints, AccumFields, Fields).

/* ======== collection_type_param_accum/3
   Accumulates the expansion of collections into references to components using the collection_field_accum/2 
   predicate accross all parameters of a product type.
*/
collection_type_param_accum(_, [], []).

collection_type_param_accum(ParentName, [Prop|AccumProperties],[Prop|Properties]) :- 
    Prop \= fields(_),
    collection_type_param_accum(ParentName, AccumProperties, Properties).

collection_type_param_accum(ParentName, [fields(AccumFields)|AccumProperties],[fields(Fields)|Properties]) :- 
    collection_field_accum(ParentName, AccumFields, Fields), 
    collection_type_param_accum(ParentName, AccumProperties, Properties).

/* ======== collection_field_accum/4
   Transforms all collections that contain multiple fields into references to top level components that contain
   the fields. Collections containing only one field are not transformed.
*/
collection_field_accum(_, [], []).

collection_field_accum(ParentName, [Field|AccumFields] , [Field|Fields]) :-
    Field \= collection(_, _, _, _),
    collection_field_accum(ParentName, AccumFields, Fields).

/* No parent, one field, direct collection. */
collection_field_accum(ParentName, 
                       [collection(CollKind, CollName, CollField)|AccumFields], 
                       [collection(CollKind, CollName, no_parent, fields([CollField]))|Fields]) :-
    collection_field_accum(ParentName, AccumFields, Fields).

/* No parent, more than one field, reference to new top-level component. */
collection_field_accum(ParentName, 
                       [collection(CollKind, CollName, component_ref(CollName, compound_name(_, CollName), _, _))|AccumFields], 
                       [collection(CollKind, CollName, no_parent, fields([_|[_|_]]))|Fields]) :-
    collection_field_accum(ParentName, AccumFields, Fields).

/* Parent, no fields, reference to self component. */
collection_field_accum(ParentName, 
                       [collection(CollKind, CollName, component_ref(ParentRef, ParentName, _, _))|AccumFields], 
                       [collection(CollKind, CollName, parent(ParentRef), fields([]))|Fields]) :-
    collection_field_accum(ParentName, AccumFields, Fields).

/* Parent, one or more fields, reference to new top-level component. */
collection_field_accum(ParentName, 
                       [collection(CollKind, CollName, component_ref(CollName, compound_name(_, CollName), _, _))|AccumFields], 
                       [collection(CollKind, CollName, parent(_), fields([_|_]))|Fields]) :-
    collection_field_accum(ParentName, AccumFields, Fields).

/* ======== promoted_collection_accum/3   
   Accumulates the expansion of collections of more than one field into top-level type declarations.
*/
promoted_collection_accum(_, [], []).

/* Skip all non-field parameters of the generating component type. */
promoted_collection_accum(ParentName, AccumComponents, [Prop|Properties]) :-
    Prop \= fields(_),
    promoted_collection_accum(ParentName, AccumComponents, Properties).

/* Extract any collections appearing in the fields. */
promoted_collection_accum(ParentName, Result, [fields(Fields)|Properties]) :-
    collection_to_component_accum(ParentName, Components, Fields),
    append(Components, AccumComponents, Result),
    promoted_collection_accum(ParentName, AccumComponents, Properties).

/* ========= collection_to_component_accum/3  
*/
collection_to_component_accum(_, [], []).

collection_to_component_accum(ParentName, Components, [Field|Fields]) :-
    Field \= collection(_, _, _, _),
    collection_to_component_accum(ParentName, Components, Fields).

/* No parent, one field, direct collection. */
collection_to_component_accum(ParentName, 
                              Components, 
                              [collection(_, _, no_parent, fields(CollFields))|Fields]) :-
    unique_field_accum(_, ExpandedCollFields, CollFields),
    ExpandedCollFields = [_],
    collection_to_component_accum(ParentName, Components, Fields).

/* No parent, more than one field, reference to new top-level component. */
collection_to_component_accum(ParentName, 
                              [top_level_component(compound_name(_, CollName), CollProperties)|Components], 
                              [collection(_, CollName, no_parent, fields(CollFields))|Fields]) :-
    unique_field_accum(CollConstraints, ExpandedCollFields, CollFields),
    ExpandedCollFields \= [_],
    append(CollConstraints, [fields(ExpandedCollFields), views([])], CollProperties),
    collection_to_component_accum(ParentName, Components, Fields).

/* Parent, no fields, reference to self component. */
collection_to_component_accum(ParentName, 
                              Components, 
                              [collection(_, _, parent(_), fields(CollFields))|Fields]) :-
    unique_field_accum(_, ExpandedCollFields, CollFields),
    ExpandedCollFields = [],
    collection_to_component_accum(ParentName, Components, Fields).

/* Parent, one or more fields, reference to new top-level component. */
collection_to_component_accum(ParentName,
                              [top_level_component(compound_name(_, CollName), CollProperties)|Components], 
                              [Field|Fields]) :-
    Field = collection(_, CollName, parent(ParentRef), fields(CollFields)),
    unique_field_accum(CollConstraints, ExpandedCollFields, CollFields),
    ExpandedCollFields \= [],
    append(CollConstraints, [fields([component_ref(ParentRef, ParentName, _, _)|ExpandedCollFields]), views([])], CollProperties),
    collection_to_component_accum(ParentName, Components, Fields).

/* Accumulates the names of properties into a list without their types or containing functors. */
property_names_accum([], []).
property_names_accum([Name|Names], [Property|Properties]) :- 
    Property = property(Name, _, _),
    property_names_accum(Names, Properties).
property_names_accum([Name|Names], [Property|Properties]) :- 
    Property = component_ref(Name, _, _, _),
    property_names_accum(Names, Properties).

/* ======== basic_type/1
   Determines whether a type is a basic type.
*/
basic_type(boolean).
basic_type(integer).
basic_type(real).
basic_type(string).
basic_type(date).
basic_type(time).
basic_type(timestamp).

/* ======== product_type/1
   Determines whether a type is a product type.
*/
product_type(view_type).
product_type(component_type).
product_type(entity_type).
product_type(dimension_type).
product_type(fact_type).

/* ======== type_check/4
   Checks that a type is either a normalized basic type, or a component type, every field of which also
   type checks. Component types must also match the types of fields specified in any views that they
   have.
*/
type_check(integer_range, MN, JT, NP) :- normal_type(integer_range, MN, JT, NP).

type_check(real_range, MN, JT, NP) :- normal_type(real_range, MN, JT, NP).

type_check(string_pattern, MN, JT, NP) :- normal_type(string_pattern, MN, JT, NP).

type_check(date_range, MN, JT, NP) :- normal_type(date_range, MN, JT, NP).

type_check(time_range, MN, JT, NP) :- normal_type(time_range, MN, JT, NP).

type_check(enumeration_type, MN, JT, NP) :- normal_type(enumeration_type, MN, JT, NP).

type_check(hierarchy_type, MN, JT, NP) :- normal_type(hierarchy_type, MN, JT, NP).

type_check(T, MN, JT, TypeProps) :- 
    product_type(T),
    normal_type(T, MN, JT, TypeProps),
    member(fields(Fields), TypeProps),
    member(views(Views), TypeProps),
    check_fields(Fields),
    conforms_to_all_views(Views, Fields).

/* ======== check_fields/1
   Checks that every field has a type that also type checks. In the case of component reference types the
   component refered to is not type checked but merely checked to exist otherwise infinite looping may occur
   where two product types hold references to each other.

   The child fields of collections and extensions are also checked.
*/
check_property(T) :- basic_type(T).
check_property(T) :- type_check(_, T, _, _).

check_fields([]).

check_fields([property(_, T, _)|FS]) :-
    check_property(T),
    check_fields(FS).

check_fields([collection(_, _, property(_, T))|FS]) :-
    check_property(T),
    check_fields(FS).

check_fields([collection(_, _, component_ref(_, CompRef, _, _))|FS]) :-
    normal_type(_, CompRef, _, _),
    check_fields(FS).

check_fields([extend(_, ExtendFields)|FS]) :-
    check_fields(ExtendFields),
    check_fields(FS).

check_fields([component_ref(_, CompRef, _, _)|FS]) :-
    normal_type(_, CompRef, _, _),
    check_fields(FS).

check_fields([unique_fields(_, _)|FS]) :-
    check_fields(FS).

/* ======== conforms_to_all_views/2
   Checks that a list of views exist and are view_types and that all fields of the views can be found
   in the fields of the component that conforms to the view.
*/
conforms_to_all_views([], _).

conforms_to_all_views([VN|VNS], Fields) :-
    normal_type(view_type, VN, _, ViewProps),
    member(fields(ViewFields), ViewProps),
    conforms_to_view(ViewFields, Fields),
    conforms_to_all_views(VNS, Fields).

conforms_to_view([], _).

conforms_to_view([VF|VFS], Fields) :-
    member(VF, Fields),
    conforms_to_view(VFS, Fields).

/* Extracts all the properties from a product type. */
properties(Properties, normal_type(_, _, _, TypeProps)) :-
    member(fields(Fields), TypeProps),
    properties_accum(Properties, Fields).

properties_accum([], []).
properties_accum(PFS, [F|FS]) :-
    (F = component_ref(_, _, _, _); F = collection(_, _, _, _); F = unique_fields(_, _); F = extend(_, _)),
    properties_accum(PFS, FS).
properties_accum([P|PFS], [P|FS]) :-
    P = property(_, _, _),
    properties_accum(PFS, FS).

/* === Entity relationships. === */

/* ======== related_uni/4
 Describes one direction of the relationship between two entities, and the property on the first entity
 which holds the relationship.
 */
related_uni(R, one, E1, E2, Prop, Owner) :-
    normal_type(entity_type, E1, _, MP1),
    normal_type(entity_type, E2, _, MP2),
    MP1 = [fields(FS1)|Props1],
    member(component_ref(Prop, E2, Owner, _), FS1).

related_uni(R, many, E1, E2, Prop, Owner) :-
    normal_type(entity_type, E1, _, MP1),
    normal_type(entity_type, E2, _, MP2),
    MP1 = [fields(FS1)|Props1],
    member(collection(_, Prop, component_ref(_, E2, Owner, _)), FS1).

/* ======== related/5
 Describes the relationship between two entities, its arity and its direction of navigability. The property
 on the first entity which holds the relationship also forms part of this relation.
 */
/* This excludes reflexive relationships with E1 \= E2. */
related(X, Y, bi, E1, E2, Prop, TProp, Owner) :-
    related_uni(X, Y, E1, E2, Prop, Owner),
    related_uni(Y, X, E2, E1, TProp, _),
    E1 \= E2.

/* This makes reflexive relationships one-to-many. */
related(many, Y, bi, E1, E2, Prop, TProp, Owner) :-
    related_uni(X, Y, E1, E2, Prop, Owner),
    related_uni(Y, X, E2, E1, TProp, _),
    E1 = E2.

related(X, Y, uni, E1, E2, Prop, TProp, Owner) :-
    related_uni(X, Y, E1, E2, Prop, Owner),
    not(related_uni(Y, X, E2, E1, TProp, _)),
    X = many.

/* ======== top_level_entity/1
 Lists all entities that are not owned by composition by a parent entity.
 */
top_level_entity(N) :-
    normal_type(entity_type, N, _, _),
    not(related(_, _, uni, _, N, _, _, O)).
