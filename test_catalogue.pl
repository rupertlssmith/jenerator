type_instance(percent, real_range, [from(0), to(100)]).
type_instance(probability, real_range, [precision(double), from(0), to(1)]).
type_instance(money, decimal_type, [precision(19), scale(4)]).
type_instance(positive, integer_range, [from(0)]).
type_instance(rating, integer_range, [from(1), to(10)]).
type_instance(email, string_pattern, [regexp("emailregexp")]).
type_instance(username, string_pattern, []).
type_instance(beyond_2000, date_range, [from("2000-01-01")]).
type_instance(twentieth_century, date_range, [from("1900-01-01"), to("1999-12-31")]).
type_instance(working_hours, time_range, [from("09:00:00"), to("17:00:00")]).
type_instance(half_hourly, time_range, [step("00:30:00")]).
type_instance(primary_colours, enumeration_type, [labels([red, green, blue])]).
type_instance(colours, enumeration_type, [labels([red, orange, green, pink])]).
type_instance(units, enumeration_type, [labels([meters, feet])]).
type_instance(order_state, enumeration_type, [labels([creating, submitted, picking, billed, dispatched, failed])]).
type_instance(taxonomy, hierarchy_type, [finalized, levels([top, supertype, subtype]), labels(label("all life", label("plant"), label("bacteria"), label("animal", label("mamal")), label("virus")))]).
type_instance(three_levels, hierarchy_type, [levels([top, main, secondary])]).
type_instance(product_hierarchy, hierarchy_type, [levels([all_products, category, subcategory])]).
type_instance(simple_view, view_type, [fields([property(test_string, string, "test_string")]), views([])]).
type_instance(simple_component, component_type, [fields([property(test_string, string, "test_string")]), views([])]).
type_instance(simple_entity, entity_type, [fields([property(test_string, string, "test_string")]), views([])]).
type_instance(measurement, component_type, [fields([property(quantity, real, "quantity"), property(units, units, "units")]), views([])]).
type_instance(address, component_type, [fields([property(house, string, "house"), property(street, string, "street"), property(town, string, "town"), property(country, string, "country"), property(postcode, string, "postcode")]), views([])]).
type_instance(billing_details, component_type, [fields([property(card_number, string, "card_number")]), views([])]).
type_instance(basic_type_entity, entity_type, [fields([property(boolean, boolean, "boolean"), property(integer, integer, "integer"), property(real, real, "real"), property(string, string, "string"), property(date, date, "date"), property(time, time, "time"), property(timestamp, timestamp, "timestamp")]), views([]), externalid]).
type_instance(name_view, view_type, [fields([property(name, string, "name")]), views([])]).
type_instance(summary_name_view, view_type, [fields([property(name, string, "name"), property(ranking, real, "ranking")]), views([])]).
type_instance(searchable_entity, entity_type, [fields([property(name, string, "name"), property(etc, string, "etc"), property(ranking, real, "ranking")]), views([summary_name_view]), externalid]).
type_instance(summary_view, view_type, [fields([property(name, string, "name"), property(type, string, "type")]), views([])]).
type_instance(brand, view_type, [fields([property(brand_description, string, "brand_description"), property(product_hierarchy, product_hierarchy, "product_hierarchy")]), views([])]).
type_instance(purchase_order, entity_type, [fields([collection(bag, line_item, no_parent, fields([component_ref(product, example_product, false, _, _), property(quantity, integer, "quantity")])), property(state, order_state, "state"), component_ref(billing, billing_details, false, _, _)]), views([completed_purchase_order])]).
type_instance(completed_purchase_order, view_type, [fields([collection(bag, line_item, no_parent, fields([component_ref(product, example_product, false, _, _), property(quantity, integer, "quantity")]))]), views([])]).
