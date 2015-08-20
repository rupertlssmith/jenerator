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
type_instance(simple_dimension, dimension_type, [fields([property(test_string, string, "test_string")]), views([])]).
type_instance(simple_fact, fact_type, [fields([property(test_string, string, "test_string")]), views([])]).
type_instance(measurement, component_type, [fields([property(quantity, real, "quantity"), property(units, units, "units")]), views([])]).
type_instance(measurement_of_b, component_type, [fields([component_ref(amount, measurement, false, _), component_ref(ref_to_b, b, false, _)]), views([])]).
type_instance(address, component_type, [fields([property(house, string, "house"), property(street, string, "street"), property(town, string, "town"), property(country, string, "country"), property(postcode, string, "postcode")]), views([])]).
type_instance(billing_details, component_type, [fields([property(card_number, string, "card_number")]), views([])]).
type_instance(basic_type_entity, entity_type, [fields([property(boolean, boolean, "boolean"), property(integer, integer, "integer"), property(real, real, "real"), property(string, string, "string"), property(date, date, "date"), property(time, time, "time"), property(timestamp, timestamp, "timestamp")]), views([]), externalid]).
type_instance(basic_type_dimension, dimension_type, [fields([property(boolean, boolean, "boolean"), property(integer, integer, "integer"), property(real, real, "real"), property(string, string, "string"), property(date, date, "date"), property(time, time, "time")]), views([]), externalid]).
type_instance(restricted_type_dimension, dimension_type, [fields([property(restricted_integer, positive, "restricted_integer"), property(restricted_real, percent, "restricted_real"), property(restricted_string, email, "restricted_string"), property(restricted_date, beyond_2000, "restricted_date"), property(restricted_time, working_hours, "restricted_time")]), views([])]).
type_instance(decimal_dimension, dimension_type, [fields([property(amount, money, "amount")]), views([])]).
type_instance(taxonomy_dimension, dimension_type, [fields([property(name, string, "name"), property(taxonomy, taxonomy, "taxonomy")]), views([name_view]), externalid]).
type_instance(custom_type_dimension, dimension_type, [fields([property(colour, colours, "colour"), property(taxonomy, taxonomy, "taxonomy")]), views([])]).
type_instance(customer, dimension_type, [fields([property(name, string, "name"), component_ref(address, address, false, _)]), views([])]).
type_instance(name_view, view_type, [fields([property(name, string, "name")]), views([])]).
type_instance(viewable_dimension, dimension_type, [fields([property(name, string, "name"), property(etc, string, "etc"), property(relevance, real, "relevance")]), views([name_view]), externalid]).
type_instance(summary_name_view, view_type, [fields([property(name, string, "name"), property(ranking, real, "ranking")]), views([])]).
type_instance(searchable_entity, entity_type, [fields([property(name, string, "name"), property(etc, string, "etc"), property(ranking, real, "ranking")]), views([summary_name_view]), externalid]).
type_instance(summary_view, view_type, [fields([property(name, string, "name"), property(type, string, "type")]), views([])]).
type_instance(natural_dimension, dimension_type, [fields([unique(key, fields([property(name, string, "name"), property(type, string, "type")])), property(etc, string, "etc")]), views([summary_view])]).
type_instance(value_dimension, dimension_type, [fields([property(name, string, "name"), property(type, string, "type"), property(etc, string, "etc")]), views([summary_view])]).
type_instance(collection_dimension, dimension_type, [fields([collection(set, set_of_things, no_parent, fields([property(thing, string, "thing")])), collection(bag, bag_of_things, no_parent, fields([property(thing, string, "thing")])), collection(list, list_of_things, no_parent, fields([property(thing, string, "thing")])), collection(map(string, key), map_of_things, no_parent, fields([property(thing, string, "thing")]))]), views([])]).
type_instance(a, dimension_type, [fields([unique(not_key, fields([component_ref(unique_ref_to_b, b, false, _)])), component_ref(ref_to_b, b, false, _), collection(set, set_unique_of_b, no_parent, fields([unique(not_key, fields([component_ref(ref_to_b, b, false, _)]))])), collection(set, set_of_b, no_parent, fields([component_ref(ref_to_b, b, false, _)])), collection(bag, bag_of_b, no_parent, fields([component_ref(ref_to_b, b, false, _)])), collection(list, list_of_b, no_parent, fields([component_ref(ref_to_b, b, false, _)])), collection(map(string, key), map_of_b, no_parent, fields([component_ref(value, b, false, _)])), component_ref(ref_to_c, c, false, _), component_ref(ref_to_d, d, false, _), collection(set, set_of_c, no_parent, fields([component_ref(ref_to_c, c, false, _)])), collection(set, set_of_d, no_parent, fields([component_ref(ref_to_d, d, false, _)])), collection(set, set_of_things_ref_back, parent(ref_to_parent), fields([])), collection(list, list_of_things_ref_back, parent(ref_to_parent), fields([])), collection(bag, bag_of_things_ref_back, parent(ref_to_parent), fields([])), collection(map(string, key), map_of_things_ref_back, parent(ref_to_parent), fields([])), collection(set, list_of_amount_of_b, no_parent, fields([property(quantity, integer, "quantity"), component_ref(ref_to_b, b, false, _)])), collection(set, list_of_measurement_of_b, no_parent, fields([component_ref(amount, measurement, false, _), component_ref(ref_to_b, b, false, _)])), collection(set, list_of_ref_to_measurement_of_b, no_parent, fields([component_ref(measurement_of_b, measurement_of_b, false, _)]))]), views([])]).
type_instance(b, dimension_type, [fields([]), views([])]).
type_instance(c, dimension_type, [fields([component_ref(ref_to_a, a, false, _)]), views([])]).
type_instance(d, dimension_type, [fields([collection(set, set_of_a, no_parent, fields([component_ref(ref_to_a, a, false, _)]))]), views([])]).
type_instance(store, dimension_type, [fields([component_ref(address, address, false, _)]), views([])]).
type_instance(example_product, dimension_type, [fields([property(product_description, string, "product_description"), unique(key, fields([property(sku, integer, "sku")])), property(brand_description, string, "brand_description"), property(product_hierarchy, product_hierarchy, "product_hierarchy")]), views([brand])]).
type_instance(brand, view_type, [fields([property(brand_description, string, "brand_description"), property(product_hierarchy, product_hierarchy, "product_hierarchy")]), views([])]).
type_instance(purchase_order, entity_type, [fields([component_ref(customer, customer, false, _), collection(bag, line_item, no_parent, fields([component_ref(product, example_product, false, _), property(quantity, integer, "quantity")])), property(state, order_state, "state"), component_ref(billing, billing_details, false, _)]), views([completed_purchase_order])]).
type_instance(completed_purchase_order, view_type, [fields([component_ref(customer, customer, false, _), collection(bag, line_item, no_parent, fields([component_ref(product, example_product, false, _), property(quantity, integer, "quantity")]))]), views([])]).
type_instance(retail_sale, component_type, [fields([component_ref(customer, customer, false, _), component_ref(store, store, false, _), collection(bag, line_item, no_parent, fields([component_ref(product, example_product, false, _), property(quantity, integer, "quantity")]))]), views([])]).
type_instance(pos_sale1, fact_type, [fields([component_ref(customer, customer, false, _), component_ref(store, store, false, _), collection(bag, line_item, no_parent, fields([component_ref(product, example_product, false, _), property(quantity, integer, "quantity")]))]), views([])]).
type_instance(pos_sale2, fact_type, [fields([component_ref(retail_sale, retail_sale, false, _)]), views([])]).
type_instance(pos_sale3, fact_type, [fields([component_ref(retail_sale, retail_sale, false, _), property(cost, real, "cost"), property(profit, real, "profit")]), views([])]).
type_instance(order1, fact_type, [fields([component_ref(purchase_order, purchase_order, false, _)]), views([])]).
type_instance(order2, fact_type, [fields([component_ref(purchase_order, purchase_order, false, _), property(date, date, "date"), property(time, time, "time")]), views([])]).
type_instance(order3, fact_type, [fields([component_ref(purchase_order, purchase_order, false, _), property(price, real, "price"), property(total_order_price, real, "total_order_price"), property(date, date, "date"), property(time, time, "time")]), views([])]).
type_instance(order_transaction, fact_type, [fields([component_ref(completed_purchase_order, completed_purchase_order, false, _), extend(extend_ref(completed_purchase_order, line_item), fields([property(price, real, "price")])), property(total_order_price, real, "total_order_price"), property(date, date, "date"), property(time, time, "time")]), views([])]).
type_instance(market_basket, fact_type, [fields([component_ref(customer, customer, false, _), component_ref(store, store, false, _), component_ref(product_a, example_product, false, _), component_ref(product_b, example_product, false, _), property(basket_count, integer, "basket_count"), property(quantity_a, integer, "quantity_a"), property(quantity_b, integer, "quantity_b"), property(price_a, real, "price_a"), property(price_b, real, "price_b")]), views([])]).
