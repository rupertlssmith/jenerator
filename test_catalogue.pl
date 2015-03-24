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
type_instance(simple_view, view_type, [views([]), fields([property(test_string, string)])]).
type_instance(simple_component, component_type, [views([]), fields([property(test_string, string)])]).
type_instance(simple_entity, entity_type, [views([]), fields([property(test_string, string)])]).
type_instance(simple_dimension, dimension_type, [views([]), fields([property(test_string, string)])]).
type_instance(simple_fact, fact_type, [views([]), fields([property(test_string, string)])]).
type_instance(measurement, component_type, [views([]), fields([property(quantity, real), property(units, units)])]).
type_instance(measurement_of_b, component_type, [views([]), fields([component_ref(amount, measurement, false), component_ref(ref_to_b, b, false)])]).
type_instance(address, component_type, [views([]), fields([property(house, string), property(street, string), property(town, string), property(country, string), property(postcode, string)])]).
type_instance(billing_details, component_type, [views([]), fields([property(card_number, string)])]).
type_instance(basic_type_entity, entity_type, [views([]), fields([property(boolean, boolean), property(integer, integer), property(real, real), property(string, string), property(date, date), property(time, time), property(timestamp, timestamp)]), externalid]).
type_instance(basic_type_dimension, dimension_type, [views([]), fields([property(boolean, boolean), property(integer, integer), property(real, real), property(string, string), property(date, date), property(time, time)]), externalid]).
type_instance(restricted_type_dimension, dimension_type, [views([]), fields([property(restricted_integer, positive), property(restricted_real, percent), property(restricted_string, email), property(restricted_date, beyond_2000), property(restricted_time, working_hours)])]).
type_instance(decimal_dimension, dimension_type, [views([]), fields([property(amount, money)])]).
type_instance(taxonomy_dimension, dimension_type, [views([name_view]), fields([property(name, string), property(taxonomy, taxonomy)]), externalid]).
type_instance(custom_type_dimension, dimension_type, [views([]), fields([property(colour, colours), property(taxonomy, taxonomy)])]).
type_instance(customer, dimension_type, [views([]), fields([property(name, string), component_ref(address, address, false)])]).
type_instance(name_view, view_type, [views([]), fields([property(name, string)])]).
type_instance(viewable_dimension, dimension_type, [views([name_view]), fields([property(name, string), property(etc, string), property(relevance, real)]), externalid]).
type_instance(summary_name_view, view_type, [views([]), fields([property(name, string), property(ranking, real)])]).
type_instance(searchable_entity, entity_type, [views([summary_name_view]), fields([property(name, string), property(etc, string), property(ranking, real)]), externalid]).
type_instance(summary_view, view_type, [views([]), fields([property(name, string), property(type, string)])]).
type_instance(natural_dimension, dimension_type, [views([summary_view]), fields([unique(key, fields([property(name, string), property(type, string)])), property(etc, string)])]).
type_instance(value_dimension, dimension_type, [views([summary_view]), fields([property(name, string), property(type, string), property(etc, string)])]).
type_instance(collection_dimension, dimension_type, [views([]), fields([collection(set, set_of_things, no_parent, fields([property(thing, string)])), collection(bag, bag_of_things, no_parent, fields([property(thing, string)])), collection(list, list_of_things, no_parent, fields([property(thing, string)])), collection(map(string, key), map_of_things, no_parent, fields([property(thing, string)]))])]).
type_instance(a, dimension_type, [views([]), fields([unique(not_key, fields([component_ref(unique_ref_to_b, b, false)])), component_ref(ref_to_b, b, false), collection(set, set_unique_of_b, no_parent, fields([unique(not_key, fields([component_ref(ref_to_b, b, false)]))])), collection(set, set_of_b, no_parent, fields([component_ref(ref_to_b, b, false)])), collection(bag, bag_of_b, no_parent, fields([component_ref(ref_to_b, b, false)])), collection(list, list_of_b, no_parent, fields([component_ref(ref_to_b, b, false)])), collection(map(string, key), map_of_b, no_parent, fields([component_ref(value, b, false)])), component_ref(ref_to_c, c, false), component_ref(ref_to_d, d, false), collection(set, set_of_c, no_parent, fields([component_ref(ref_to_c, c, false)])), collection(set, set_of_d, no_parent, fields([component_ref(ref_to_d, d, false)])), collection(set, set_of_things_ref_back, parent(ref_to_parent), fields([])), collection(list, list_of_things_ref_back, parent(ref_to_parent), fields([])), collection(bag, bag_of_things_ref_back, parent(ref_to_parent), fields([])), collection(map(string, key), map_of_things_ref_back, parent(ref_to_parent), fields([])), collection(set, list_of_amount_of_b, no_parent, fields([property(quantity, integer), component_ref(ref_to_b, b, false)])), collection(set, list_of_measurement_of_b, no_parent, fields([component_ref(amount, measurement, false), component_ref(ref_to_b, b, false)])), collection(set, list_of_ref_to_measurement_of_b, no_parent, fields([component_ref(measurement_of_b, measurement_of_b, false)]))])]).
type_instance(b, dimension_type, [views([]), fields([])]).
type_instance(c, dimension_type, [views([]), fields([component_ref(ref_to_a, a, false)])]).
type_instance(d, dimension_type, [views([]), fields([collection(set, set_of_a, no_parent, fields([component_ref(ref_to_a, a, false)]))])]).
type_instance(store, dimension_type, [views([]), fields([component_ref(address, address, false)])]).
type_instance(example_product, dimension_type, [views([brand]), fields([property(product_description, string), unique(key, fields([property(sku, integer)])), property(brand_description, string), property(product_hierarchy, product_hierarchy)])]).
type_instance(brand, view_type, [views([]), fields([property(brand_description, string), property(product_hierarchy, product_hierarchy)])]).
type_instance(purchase_order, entity_type, [views([completed_purchase_order]), fields([component_ref(customer, customer, false), collection(bag, line_item, no_parent, fields([component_ref(product, example_product, false), property(quantity, integer)])), property(state, order_state), component_ref(billing, billing_details, false)])]).
type_instance(completed_purchase_order, view_type, [views([]), fields([component_ref(customer, customer, false), collection(bag, line_item, no_parent, fields([component_ref(product, example_product, false), property(quantity, integer)]))])]).
type_instance(retail_sale, component_type, [views([]), fields([component_ref(customer, customer, false), component_ref(store, store, false), collection(bag, line_item, no_parent, fields([component_ref(product, example_product, false), property(quantity, integer)]))])]).
type_instance(pos_sale1, fact_type, [views([]), fields([component_ref(customer, customer, false), component_ref(store, store, false), collection(bag, line_item, no_parent, fields([component_ref(product, example_product, false), property(quantity, integer)]))])]).
type_instance(pos_sale2, fact_type, [views([]), fields([component_ref(retail_sale, retail_sale, false)])]).
type_instance(pos_sale3, fact_type, [views([]), fields([component_ref(retail_sale, retail_sale, false), property(cost, real), property(profit, real)])]).
type_instance(order1, fact_type, [views([]), fields([component_ref(purchase_order, purchase_order, false)])]).
type_instance(order2, fact_type, [views([]), fields([component_ref(purchase_order, purchase_order, false), property(date, date), property(time, time)])]).
type_instance(order3, fact_type, [views([]), fields([component_ref(purchase_order, purchase_order, false), property(price, real), property(total_order_price, real), property(date, date), property(time, time)])]).
type_instance(order_transaction, fact_type, [views([]), fields([component_ref(completed_purchase_order, completed_purchase_order, false), extend(extend_ref(completed_purchase_order, line_item), fields([property(price, real)])), property(total_order_price, real), property(date, date), property(time, time)])]).
type_instance(market_basket, fact_type, [views([]), fields([component_ref(customer, customer, false), component_ref(store, store, false), component_ref(product_a, example_product, false), component_ref(product_b, example_product, false), property(basket_count, integer), property(quantity_a, integer), property(quantity_b, integer), property(price_a, real), property(price_b, real)])]).
