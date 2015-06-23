package org.modelcatalogue.core.util.builder

import grails.util.GrailsNameUtils
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.util.logging.Log4j
import org.modelcatalogue.core.*

/**
 * CatalogueBuilder class allows to design the catalogue elements relationship in a tree-like structure simply without
 * having to know the implementation details. CatalogueBuilder handles creating the draft versions if necessary.
 *
 * Practical example how the builder can be used are the imports present in the application or DSL MC files.
 *
 * @see CatalogueBuilderScript
 */
interface CatalogueBuilder extends ExtensionAwareBuilder {

    /**
     * Builds catalogue elements based on the DSL method call inside the closure.
     *
     * First it builds the model of catalogue elements in memory than it stores the changes in the database and
     * reset the state of the builder to the default (but the <code>skipDrafts</code> flag remains the same).
     *
     * @param c catalogue definition
     * @return set of resolved elements
     */
    Set<CatalogueElement> build(@DelegatesTo(CatalogueBuilder) Closure c)

    /**
     * @see #classification(java.util.Map, Closure)
     */
    CatalogueElementProxy<Classification> classification(Map<String, Object> parameters)

    /**
     * Creates new classification, reuses the latest draft or creates new draft unless the exactly same classification
     * already exists in the catalogue. Accepts any bindable parameters which Classification instances does.
     *
     * All elements resolved from DSL method calls inside the closure provided will be classified by this classification
     * automatically. This is the reason why most of the use cases will uses this classification method call as top
     * level call.
     *
     * @param parameters map of parameters such as name or id
     * @param c DSL definition closure
     * @return proxy to classification specified by the parameters map and the DSL closure
     */
    CatalogueElementProxy<Classification> classification(Map<String, Object> parameters, @DelegatesTo(CatalogueBuilder) Closure c)

    /**
     * Creates new model, reuses the latest draft or creates new draft unless the exactly same model already exists
     * in the catalogue. Accepts any bindable parameters which Model instances does.
     *
     * Models nested inside the DSL definition closure will be set as child models for this model.
     * Data elements nested inside the DSL definition closure will be set as contained elements for this model.
     *
     * @param parameters map of parameters such as name or id
     * @param c DSL definition closure
     * @return proxy to model specified by the parameters map and the DSL closure
     */
    CatalogueElementProxy<Model> model(Map<String, Object> parameters, @DelegatesTo(CatalogueBuilder) Closure c)

    /**
     * @see #model(java.util.Map, groovy.lang.Closure)
     */
    CatalogueElementProxy<Model> model(Map<String, Object> parameters)

    /**
     * Creates new data element, reuses the latest draft or creates new draft unless the exactly same data element
     * already exists in the catalogue. Accepts any bindable parameters which DataElement instances does.
     *
     * Value domain nested inside the DSL definition closure will be set as value domain of this element.
     *
     * If value domains are created automatically a value domain with the same name and description will be created
     * if no nested value domain is specified.
     *
     * @param parameters map of parameters such as name or id
     * @param c DSL definition closure
     * @return proxy to data element specified by the parameters map and the DSL closure
     */
    CatalogueElementProxy<DataElement> dataElement(Map<String, Object> parameters, @DelegatesTo(CatalogueBuilder) Closure c)

    /**
     * @see #dataElement(java.util.Map, groovy.lang.Closure)
     */
    CatalogueElementProxy<DataElement> dataElement(Map<String, Object> parameters)

    /**
     * Creates new value domain, reuses the latest draft or creates new draft unless the exactly same value domain
     * already exists in the catalogue. Accepts any bindable parameters which ValueDomain instances does.
     *
     * Measurement unit nested inside the DSL definition closure will be set as unit of measure of this element.
     * Data type nested inside the DSL definition closure will be set as data type of this element.
     *
     * If data types are created automatically a data type with the same name and description will be created
     * if no nested data type is specified.
     *
     * @param parameters map of parameters such as name or id
     * @param c DSL definition closure
     * @return proxy to value domain specified by the parameters map and the DSL closure
     */
    CatalogueElementProxy<ValueDomain> valueDomain(Map<String, Object> parameters, @DelegatesTo(CatalogueBuilder) Closure c)

    /**
     * see #valueDomain(java.util.Map, groovy.lang.Closure)
     */
    CatalogueElementProxy<ValueDomain> valueDomain(Map<String, Object> parameters)

    /**
     * see #valueDomain(java.util.Map, groovy.lang.Closure)
     */
    CatalogueElementProxy<ValueDomain> valueDomain()

    /**
     * Creates new data type, reuses the latest draft or creates new draft unless the exactly same data type
     * already exists in the catalogue. Accepts any bindable parameters which DataType instances does.
     *
     * @param parameters map of parameters such as name or id
     * @param c DSL definition closure
     * @return proxy to data type specified by the parameters map and the DSL closure
     */
    CatalogueElementProxy<? extends DataType> dataType(Map<String, Object> parameters, @DelegatesTo(CatalogueBuilder) Closure c)

    /**
     * see #dataType(java.util.Map, groovy.lang.Closure)
     */
    CatalogueElementProxy<? extends DataType> dataType(Map<String, Object> parameters)

    /**
     * see #dataType(java.util.Map, groovy.lang.Closure)
     */
    CatalogueElementProxy<? extends DataType> dataType()

    /**
     * Creates new measurement unit, reuses the latest draft or creates new draft unless the exactly same measurement
     * unit already exists in the catalogue. Accepts any bindable parameters which MeasurementUnit instances does.
     *
     * @param parameters map of parameters such as name or id
     * @param c DSL definition closure
     * @return proxy to measurement unit specified by the parameters map and the DSL closure
     */
    CatalogueElementProxy<MeasurementUnit> measurementUnit(Map<String, Object> parameters, @DelegatesTo(CatalogueBuilder) Closure c)

    /**
     * see #measurementUnit(java.util.Map, groovy.lang.Closure)
     */
    CatalogueElementProxy<MeasurementUnit> measurementUnit(Map<String, Object> parameters)

    /**
     * Configures the relationships created automatically for nested elements such as the containment relationship
     * for data element nested in model.
     *
     * Primary use case for this method call is to configure the relationship metadata such as "Min. Occurs".
     *
     * @param relationshipExtensionsConfiguration DSL definition closure expecting setting the relationship metadata
     * @see RelationshipProxyConfiguration
     */
    void relationship(@DelegatesTo(RelationshipProxyConfiguration) Closure relationshipExtensionsConfiguration)

    /**
     * Adds the model specified by given classification and name to a parent model.
     *
     * Metadata for this hierarchy relationship can be set using the extensions DSL definition closure.
     *
     * @param classification classification of the child model
     * @param name name of the child model
     * @param extensions DSL definition closure expecting setting the relationship metadata
     * @see RelationshipProxyConfiguration
     */
    void child(String classification, String name, @DelegatesTo(RelationshipProxyConfiguration) Closure extensions)

    /**
     * see #child(String, String, Closure)
     */
    void child(String classification, String name)

    /**
     * Adds the model specified by given name to the parent model. The model is searched within the parent
     * classification if not global search for models set.
     *
     * Metadata for this hierarchy relationship can be set using the extensions DSL definition closure.
     *
     * @param name name of the child model
     * @param extensions DSL definition closure expecting setting the relationship metadata
     * @see RelationshipProxyConfiguration
     * @see #globalSearchFor(java.lang.Class)
     */
    void child(String name, @DelegatesTo(RelationshipProxyConfiguration) Closure extensions)

    /**
     * see #child(String, Closure)
     */
    void child(String name)

    /**
     * Adds the model specified by given proxy to the parent model.
     *
     * Metadata for this hierarchy relationship can be set using the extensions DSL definition closure.
     *
     * @param model proxy of the child model
     * @param extensions DSL definition closure expecting setting the relationship metadata
     * @see RelationshipProxyConfiguration
     * @see #globalSearchFor(java.lang.Class)
     */
    void child(CatalogueElementProxy<Model> model, @DelegatesTo(RelationshipProxyConfiguration) Closure extensions)

    /**
     * see #child(org.modelcatalogue.core.util.builder.CatalogueElementProxy, Closure)
     */
    void child(CatalogueElementProxy<Model> model)

    /**
     * Adds the data element specified by given classification and name to a parent model.
     *
     * Metadata for this hierarchy relationship can be set using the extensions DSL definition closure.
     *
     * @param classification classification of the contained data element
     * @param name name of the contained data element
     * @param extensions DSL definition closure expecting setting the relationship metadata
     * @see RelationshipProxyConfiguration
     */
    void contains(String classification, String name, @DelegatesTo(RelationshipProxyConfiguration) Closure extensions)

    /**
     * see #contains(String, String, Closure)
     */
    void contains(String classification, String name)

    /**
     * Adds the data element specified by given name to the parent model. The data element is searched within the parent
     * classification if not global search for data elements set.
     *
     * Metadata for this hierarchy relationship can be set using the extensions DSL definition closure.
     *
     * @param name name of the contained data element
     * @param extensions DSL definition closure expecting setting the relationship metadata
     * @see RelationshipProxyConfiguration
     * @see #globalSearchFor(java.lang.Class)
     */
    void contains(String name, @DelegatesTo(RelationshipProxyConfiguration) Closure extensions)

    /**
     * see #contains(String, Closure)
     */
    void contains(String name)

    /**
     * Adds the data element specified by given proxy to the parent model.
     *
     * Metadata for this hierarchy relationship can be set using the extensions DSL definition closure.
     *
     * @param model proxy of the contained data element
     * @param extensions DSL definition closure expecting setting the relationship metadata
     * @see RelationshipProxyConfiguration
     * @see #globalSearchFor(java.lang.Class)
     */
    void contains(CatalogueElementProxy<DataElement> element, @DelegatesTo(RelationshipProxyConfiguration) Closure extensions)

    /**
     * see #contains(org.modelcatalogue.core.util.builder.CatalogueElementProxy, Closure)
     */
    void contains(CatalogueElementProxy<DataElement> element)

    /**
     * Adds the base element specified by given classification and name to a parent element.
     *
     * Metadata for this hierarchy relationship can be set using the extensions DSL definition closure.
     *
     * @param classification classification of the base element
     * @param name name of the base element
     * @param extensions DSL definition closure expecting setting the relationship metadata
     * @see RelationshipProxyConfiguration
     */
    void basedOn(String classification, String name, @DelegatesTo(RelationshipProxyConfiguration) Closure extensions)

    /**
     * see #basedOn(String, String, Closure)
     */
    void basedOn(String classification, String name)

    /**
     * Adds the base element specified by given name to the parent element. The model is searched within the parent
     * classification if not global search for the particular element set.
     *
     * Metadata for this hierarchy relationship can be set using the extensions DSL definition closure.
     *
     * @param name name of the base element
     * @param extensions DSL definition closure expecting setting the relationship metadata
     * @see RelationshipProxyConfiguration
     * @see #globalSearchFor(java.lang.Class)
     */
    void basedOn(String name, @DelegatesTo(RelationshipProxyConfiguration) Closure extensions)

    /**
     * see #basedOn(String, Closure)
     */
    void basedOn(String name)

    /**
     * Adds the base element specified by given proxy to the parent element.
     *
     * Metadata for this hierarchy relationship can be set using the extensions DSL definition closure.
     *
     * @param model proxy of the base element
     * @param extensions DSL definition closure expecting setting the relationship metadata
     * @see RelationshipProxyConfiguration
     * @see #globalSearchFor(java.lang.Class)
     */
    void basedOn(CatalogueElementProxy<CatalogueElement> element, @DelegatesTo(RelationshipProxyConfiguration) Closure extensions )

    /**
     * see #basedOn(org.modelcatalogue.core.util.builder.CatalogueElementProxy, Closure)
     */
    void basedOn(CatalogueElementProxy<CatalogueElement> element)

    /**
     * Assigns the id of the element dynamically.
     *
     * The builder closure should take two parameters a String name and Class type and transform them into String id
     * which must be valid URL.
     *
     * @param idBuilder builder closure
     */
    void id(@DelegatesTo(CatalogueBuilder) @ClosureParams(value=FromString, options=['String,Class']) Closure<String> idBuilder)

    /**
     * Allows to create a catalogue element proxy only from given ID.
     * @param id ID of the target of the proxy created
     * @return proxy specified by given ID
     */
    CatalogueElementProxy<? extends CatalogueElement> ref(String id)

    /**
     * Creates new relationship builder for given relationship type specified by name.
     * @param relationshipTypeName name of the relationship type
     * @return the builder for given relationship type
     * @see RelationshipBuilder
     */
    RelationshipBuilder rel(String relationshipTypeName)

    /**
     * Sets the description of element.
     * @param description description of element
     */
    void description(String description)

    /**
     * Sets the rule of the value domain. Fails if not inside value domain definition or any other catalogue element
     * having the rule property.
     * @param rule rule of the parent value domain
     */
    void rule(String rule)

    /**
     * Sets the regular expression rule of the value domain. Fails if the current is not a value domain or any other
     * catalogue element supporting setting the regular expression rule property.
     * @param rule rule of the parent value domain
     */
    void regex(String regex)

    /**
     * Sets the model catalogue id of the current element. The id must be a valid URL.
     * @param id id which must be valid URL
     * @see #id(groovy.lang.Closure)
     */
    void id(String id)

    /**
     * Sets the status of the current element. Currently it does not work as expected as it sets the status property
     * right after object is resolved instead e.g. finalizing the element after all the work is done.
     * @param status new status of the element
     */
    void status(ElementStatus status)

    /**
     * Sets the extension (metadata) for current element from given key and value pair.
     *
     * For setting the extensions (metadata) of relationship between current element and its parent element
     * use #relationship(Closure).
     *
     * @param key metadata key
     * @param value metadata value
     */
    void ext(String key, String value)

    /**
     * Sets the extensions (metadata) for parent element from given map.
     *
     * For setting the extensions (metadata) of relationship between current element and its parent element
     * use #relationship(Closure).
     *
     * @param values metadata
     */
    void ext(Map<String, String> values)

    /**
     * Disables the dirty-checking during the builder calls for faster imports when it's known that all the changes
     * are desired (i.e. importing into an empty catalogue).
     * @param draft must be "draft" or ElementStatus#DRAFT
     */
    void skip(ElementStatus draft)

    /**
     * Sets the flag to copy relationships when draft element is created (e.g. there is an update for element).
     *
     * Normally the relationships are skipped when creating draft and the builder is supposed to rebuilt them
     * (for example in XML or MC files) but sometimes the it's desired to copy the relationships as well (e.g.
     * Excel import which usually provides incomplete data).
     *
     * @param relationships must be "relationships" string (#getRelationships() shortcut can be used)
     */
    void copy(String relationships)

    /**
     * Shortcut for Classification type so it does not have to me imported into the DSL scripts.
     * @return Classification type
     */
    Class<Classification> getClassification()

    /**
     * Shortcut for Model type so it does not have to me imported into the DSL scripts.
     * @return Model type
     */
    Class<Model> getModel()

    /**
     * Shortcut for DataElement type so it does not have to me imported into the DSL scripts.
     * @return DataElement type
     */
    Class<DataElement> getDataElement()

    /**
     * Shortcut for ValueDomain type so it does not have to me imported into the DSL scripts.
     * @return ValueDomain type
     */
    Class<ValueDomain> getValueDomain()

    /**
     * Shortcut for DataType type so it does not have to me imported into the DSL scripts.
     * @return DataType type
     */
    Class<DataType> getDataType()

    /**
     * Shortcut for DataType type so it does not have to me imported into the DSL scripts.
     * @return DataType type
     */
    Class<MeasurementUnit> getMeasurementUnit()

    /**
     * Shortcut for ElementStatus#DRAFT type so it does not have to me imported into the DSL scripts.
     * @return ElementStatus#DRAFT
     */
    ElementStatus getDraft()

    /**
     * Shortcut for ElementStatus#DEPRECATED type so it does not have to me imported into the DSL scripts.
     * @return ElementStatus#DEPRECATED
     */
    ElementStatus getDeprecated()

    /**
     * Shortcut for ElementStatus#DEPRECATED type so it does not have to me imported into the DSL scripts.
     * @return ElementStatus#DEPRECATED
     */
    ElementStatus getFinalized()

    /**
     * Keyword to be used with #copy(String) method.
     * @return string "relationships"
     */
    String getRelationships()

    /**
     * Sets the flag to be able to search for elements having no classification at all even the classification is
     * required in other situations.
     *
     * This is mainly for fixing the classifications in older database. Let's say you have a value domain "speed"
     * without any classification and you would like to be matched by name inside the "Car" classification than you will
     * use <code>globalSearchFor measurementUnit</code> declaration nested inside that "Car" classification so if the
     * "speed" domain is not found within "Car" classification but if there is a "speed" domain without any
     * classification that domain will be matched.
     *
     * This setting only applies within one classification DSL closure.
     *
     * Measurement units has the unclassified search enabled by default.
     *
     * @param type type for unclassified searches
     */
    public <T extends CatalogueElement> void globalSearchFor(Class<T> type)

    /**
     * Trigger the automatic creation of nested elements of given types. Currently data types and value domains are
     * supported for automatic creation.
     *
     * If for example automatic creation of value domains is set and there is no nested <code>valueDomain</code> call
     * inside <code>dataElement</code> DSL definition closure value domain is created or matched with the name
     * of the data element and its description. Use this feature in imports when imported data don't have a concept
     * of value domains or data types to create value domains and data types placeholders automatically.
     *
     * You can set both supported classes calling this method twice.
     *
     * @param type either dataType or valueDomain
     */
    public <T extends CatalogueElement> void automatic(Class<T> type)

    /**
     * Returns the set of elements resolve by latest call to build method. They don't have to be newly created if there were matched
     * against existing elements in the database. The set is exactly the same as the one returned from the
     * #build(Closure) method.
     *
     * @return set of elements resolve by the latest call to the build method
     */
    public Set<CatalogueElement> getLastCreated()

}

