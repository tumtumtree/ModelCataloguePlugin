package uk.co.mc.core

import grails.converters.JSON
import grails.util.GrailsNameUtils
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.codehaus.groovy.grails.plugins.web.mimes.MimeTypesFactoryBean
import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification
import spock.lang.Unroll
import uk.co.mc.core.fixtures.MockFixturesLoader
import uk.co.mc.core.util.marshalling.AbstractMarshallers

import javax.servlet.http.HttpServletResponse

/**
 * Abstract parent for restful controllers specification.
 *
 * The concrete subclass must use {@link grails.test.mixin.web.ControllerUnitTestMixin}.
 */
abstract class AbstractRestfulControllerSpec<T> extends Specification {

    private static final int DUMMY_ENTITIES_COUNT = 12

    def newInstance, badInstance, propertiesToCheck, propertiesToEdit, loadItem2, loadItem1

    MockFixturesLoader fixturesLoader = new MockFixturesLoader()

    def setup() {
        setupMimeTypes()
        marshallers.each { it.register() }
    }

    protected void setupMimeTypes() {
        def ga = grailsApplication
        ga.config.grails.mime.types =
                [html: ['text/html', 'application/xhtml+xml'],
                        xml: ['text/xml', 'application/xml'],
                        text: 'text/plain',
                        js: 'text/javascript',
                        rss: 'application/rss+xml',
                        atom: 'application/atom+xml',
                        css: 'text/css',
                        csv: 'text/csv',
                        all: '*/*',
                        json: ['application/json', 'text/json'],
                        form: 'application/x-www-form-urlencoded',
                        multipartForm: 'multipart/form-data'
                ]

        defineBeans {
            mimeTypes(MimeTypesFactoryBean) {
                grailsApplication = ga
            }
        }
    }

    /**
     * Records the given json as fixture returning the file created or updated.
     *
     * The json will be available as <code>fixtures.resourceName.fixtureName</code> variable.
     *
     * @param fixtureName name of the fixture variable and the file holding it as well
     * @param json json to be saved to the fixture
     */
    protected File recordResult(String fixtureName, JSONElement json) {
        File fixtureFile = new File("../ModelCatalogueCorePlugin/test/js/modelcatalogue/core/$controller.resourceName/${fixtureName}.gen.fixture.js")
        fixtureFile.parentFile.mkdirs()
        fixtureFile.text = """/** Generated automatically from ${getClass()}. Do not edit this file manually! */
(function (window) {
    window['fixtures'] = window['fixtures'] || {};
    var fixtures = window['fixtures']
    fixtures['$controller.resourceName'] = fixtures['$controller.resourceName'] || {};
    var $controller.resourceName = fixtures['$controller.resourceName']

    window.fixtures.${controller.resourceName}.$fixtureName = ${new JSON(json).toString(true)}

})(window)"""
        println "New fixture file created at $fixtureFile.canonicalPath"
        fixtureFile
    }

    /**
     * Records the given xml text as fixture returning the file created or updated.
     *
     * @param fixtureName name of the fixture variable and the file holding it as well
     * @param xml xml to be saved to the fixture
     */
    protected File recordResult(String fixtureName, GPathResult xml) {
        File fixtureFile = new File("../ModelCatalogueCorePlugin/target/xml-samples/modelcatalogue/core/$controller.resourceName/${fixtureName}.gen.xml")
        fixtureFile.parentFile.mkdirs()
        fixtureFile.text = """${
            XmlUtil.serialize(xml).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        }"""
        println "New xml file created at $fixtureFile.canonicalPath"
        fixtureFile
    }


    /**
     * Records the given xml text as fixture returning the file created or updated.
     *
     * @param fixtureName name of the fixture variable and the file holding it as well
     * @param xml xml string to be saved to the fixture
     */
    protected File recordInput(String fixtureName, String xml) {
        File fixtureFile = new File("../ModelCatalogueCorePlugin/target/xml-samples/modelcatalogue/core/$controller.resourceName/${fixtureName}.gen.xml")
        fixtureFile.parentFile.mkdirs()
        fixtureFile.text = """${
            XmlUtil.serialize(xml).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        }"""
        println "New xml file created at $fixtureFile.canonicalPath"
        fixtureFile
    }


    @Unroll
    def "list items test: #no where max: #max offset: #offset"() {
        fillWithDummyEntities()

        expect:
        resource.count() == total


        when:
        response.format = "json"
        params.max = max
        params.offset = offset

        controller.index()
        JSONElement json = response.json


        recordResult "list${no}", json

        then:

        json.success
        json.size == size
        json.total == total
        json.list
        json.list.size() == size
        json.next == next
        json.previous == previous



        where:

        no | size | max | offset | total | next                                 | previous
        1  | 10   | 10  | 0      | 12    | "/${resourceName}/?max=10&offset=10" | ""
        2  | 5    | 5   | 0      | 12    | "/${resourceName}/?max=5&offset=5"   | ""
        3  | 5    | 5   | 5      | 12    | "/${resourceName}/?max=5&offset=10"  | "/${resourceName}/?max=5&offset=0"
        4  | 4    | 4   | 8      | 12    | ""                                   | "/${resourceName}/?max=4&offset=4"
        5  | 2    | 10  | 10     | 12    | ""                                   | "/${resourceName}/?max=10&offset=0"
        6  | 2    | 2   | 10     | 12    | ""                                   | "/${resourceName}/?max=2&offset=8"

    }

    String getResourceName() {
        GrailsNameUtils.getLogicalPropertyName(getClass().getSimpleName(), "ControllerSpec")
    }


    def "Show single existing item as JSON"() {

        when:
        response.format = "json"

        params.id = "${loadItem1.id}"

        controller.show()

        JSONObject json = response.json

        recordResult 'showOne', json

        then:
        json
        json.id == loadItem1.id
        json.version == loadItem1.version
        json.outgoingRelationships == [count: 1, link: "/${resourceName}/outgoing/${loadItem1.id}"]
        json.incomingRelationships == [count: 0, link: "/${resourceName}/incoming/${loadItem1.id}"]

        jsonPropertyCheck(json, loadItem1)



    }




    def "Do not create new instance from JSON with bad json name"() {
        expect:
        !resource.findByName("b"*256)

        when:
        response.format = "json"
        request.json = badInstance.properties

        controller.save()

        JSONElement created = response.json
        def stored = resource.findByName("b"*256)

        recordResult 'saveErrors', created

        then:
        !stored
        created
        created.errors
        created.errors.size() >= 1
        created.errors.first().field == 'name'
    }

    def "Create new instance from JSON"() {
        expect:
        !resource.findByName(newInstance.name)

        when:
        response.format = "json"
        request.json = newInstance.properties

        controller.save()

        JSONObject created = response.json
        def stored = resource.findByName(newInstance.name)

        recordResult 'saveOk', created

        then:
        stored
        created
        created.id == stored.id
        created.version == stored.version
        jsonPropertyCheck(created, newInstance)
    }


    def "Create new instance from XML"() {
        expect:
        !resource.findByName(newInstance.name)

        when:
        response.format = "xml"

        def xml =  newInstance.encodeAsXML()

        recordInput("createInput", xml)

        request.xml = xml

        controller.save()

        GPathResult created = response.xml
        def stored = resource.findByName(newInstance.name)

        recordResult("createOk", created)

        then:
        stored
        created
        created.@id == stored.id
        created.@version == stored.version

        xmlPropertyCheck(created, newInstance)

    }


    def "Show single existing item as XML"() {
        response.format = "xml"

        params.id = "${loadItem1.id}"

        controller.show()

        GPathResult xml = response.xml
        recordResult("showOne", xml)

        expect:
        xml
        xml.@id == loadItem1.id
        xml.@version == loadItem1.version
        xml.outgoingRelationships.@count == 1
        xml.outgoingRelationships.@link == "/${resourceName}/outgoing/${loadItem1.id}"
        xml.incomingRelationships.@count == 0
        xml.incomingRelationships.@link == "/${resourceName}/incoming/${loadItem1.id}"

        xmlPropertyCheck(xml, loadItem1)

    }

    def "edit instance description from JSON"() {
        def instance = resource.findByName(loadItem1.name)

        expect:
        instance

        when:
        response.format = "json"
        params.id = instance.id
        request.json = [description: "blah blah blah blah"]

        controller.update()

        JSONObject updated = response.json

        recordResult 'updateOk', updated

        then:
        updated
        updated.id == instance.id
        updated.version == instance.version
        jsonPropertyCheck(updated, loadItem1)

    }

    def "edit instance from XML"() {
        def instance = resource.findByName(loadItem1.name)

        expect:
        instance

        when:
        response.format = "xml"
        params.id = instance.id

        loadItem1.properties = propertiesToEdit

        request.xml = loadItem1.encodeAsXML()

        recordInput("updateInput", xml)

        controller.update()

        GPathResult updated = response.xml

        recordResult 'updateOk', updated

        then:
        updated
        updated.@id == instance.id
        updated.@version == instance.version
        xmlPropertyCheck(updated, loadItem1)

    }

    def "Do not create new instance with bad XML"() {
        expect:
        !resource.findByName("")

        when:
        response.format = "xml"
        def xml = badInstance.encodeAsXML()
        request.xml = xml

        recordInput("saveErrorsInput", xml)

        controller.save()

        GPathResult created = response.xml
        def stored = resource.findByName("")

        recordResult 'saveErrors', created

        then:
        !stored
        created
        created == "Property [name] of class [class uk.co.mc.core.${resourceName.capitalize()}] cannot be null"
    }

    def "edit instance with bad JSON name"() {
        def instance = resource.findByName(loadItem1.name)

        expect:
        instance

        when:
        response.format = "json"
        params.id = instance.id
        request.json = [name: "g" * 256]

        controller.update()

        JSONObject updated = response.json

        recordResult 'updateErrors', updated

        then:
        updated
        updated.errors
        updated.errors.size() == 1
        updated.errors.first().field == 'name'


    }

    def "edit instance with bad XML"() {
        def instance = resource.findByName(loadItem1.name)

        expect:
        instance

        when:
        response.format = "xml"
        params.id = instance.id
        def xml = badInstance.encodeAsXML()

        recordInput("updateErrorsInput", xml)

        request.xml = xml

        controller.update()

        GPathResult updated = response.xml

        recordResult 'updateErrors', updated

        then:
        updated
        updated== "Property [name] of class [class uk.co.mc.core.${resourceName.capitalize()}] cannot be null"



    }

    def "Return 404 for non-existing item as JSON"() {
        response.format = "json"

        params.id = "1000000"

        controller.show()

        expect:
        response.text == ""
        response.status == HttpServletResponse.SC_NOT_FOUND
    }

    def "Return 404 for non-existing item as XML"() {
        response.format = "xml"

        params.id = "1000000"

        controller.show()

        expect:
        response.text == ""
        response.status == HttpServletResponse.SC_NOT_FOUND
    }

    def "Return 404 for non-existing item as JSON for incoming relationships"() {
        response.format = "json"

        params.id = "1000000"

        controller.incoming(10)

        expect:
        response.text == ""
        response.status == HttpServletResponse.SC_NOT_FOUND
    }

    def "Return 404 for non-existing item as XML for incoming relationships"() {
        response.format = "xml"

        params.id = "1000000"

        controller.incoming(10)

        expect:
        response.text == ""
        response.status == HttpServletResponse.SC_NOT_FOUND
    }

    def "Return 404 for non-existing item as JSON for outgoing relationships"() {
        response.format = "json"

        params.id = "1000000"

        controller.outgoing(10)

        expect:
        response.text == ""
        response.status == HttpServletResponse.SC_NOT_FOUND
    }

    def "Return 404 for non-existing item as XML for outgoing relationships"() {
        response.format = "xml"

        params.id = "1000000"

        controller.outgoing(10)

        expect:
        response.text == ""
        response.status == HttpServletResponse.SC_NOT_FOUND
    }

    def "Return 404 for non-existing item as JSON on delete"() {
        response.format = "json"

        params.id = "1000000"

        controller.delete()

        expect:
        response.text == ""
        response.status == HttpServletResponse.SC_NOT_FOUND
    }

    def "Return 404 for non-existing item as XML on delete"() {
        response.format = "xml"

        params.id = "1000000"

        controller.delete()

        expect:
        response.text == ""
        response.status == HttpServletResponse.SC_NOT_FOUND
    }


    def "Return 204 for existing item as JSON on delete"() {
        response.format = "json"

        params.id = "1"

        controller.delete()

        expect:
        response.text == ""
        response.status == HttpServletResponse.SC_NO_CONTENT
        !resource.get(params.id)
    }

    def "Return 204 for existing item as XML on delete"() {
        response.format = "xml"

        params.id = "1"

        controller.delete()

        expect:
        response.text == ""
        response.status == HttpServletResponse.SC_NO_CONTENT
        !resource.get(params.id)
    }


    boolean jsonPropertyCheck(json, loadItem){
        for (int j = 0; (j < propertiesToCheck.size()); j++) {
            def property = propertiesToCheck[j]
            property = property.toString().replaceAll("\\@", "")
            def subProperties = property.split("\\.")
            def jsonProp = json
            def loadProp = loadItem

            if(subProperties.size()>1){
                for( int i = 0 ; (i < subProperties.size()) ; i++){
                    def subProperty = subProperties[i]
                    jsonProp = jsonProp[subProperty]
                    loadProp = loadProp.getProperty(subProperty)
                }
            }else{
                jsonProp = json[property]
                loadProp = loadItem.getProperty(property)
            }

            if (jsonProp.toString() != loadProp.toString()) {
                throw new AssertionError("error: property to check: ${propertiesToCheck[j]}  where json:${jsonProp} !=  item:${loadProp}")
            }
        }

        return true

    }

    boolean xmlPropertyCheck(xml, loadItem){
        for (int j = 0; (j < propertiesToCheck.size()); j++) {
            def property = propertiesToCheck[j]
            def subProperties = property.toString().split("\\.")
            def xmlProp = xml
            def loadProp = loadItem

            if(subProperties.size()>1){
                for( int i = 0 ; (i < subProperties.size()) ; i++){
                    def subProperty = subProperties[i]
                    if(subProperty.contains("@")){
                        xmlProp = xmlProp[subProperty]
                        subProperty = subProperty.replaceAll("\\@", "")
                        loadProp = loadProp.getProperty(subProperty)
                    }else{
                        xmlProp = xmlProp[subProperty]
                        loadProp = loadProp.getProperty(subProperty)
                    }
                }
            }else{
                xmlProp = xml[property]
                loadProp = loadItem.getProperty(property.toString().replaceAll("\\@", ""))
            }

            if (xmlProp != loadProp) {
                throw new AssertionError("error: property to check: ${propertiesToCheck[j]}  where xml:${xmlProp} !=  item:${loadProp}")
            }
        }
        return true

    }



    def cleanup() {
        resource.deleteAll(resource.list())
    }

    void fillWithDummyEntities(int limit = DUMMY_ENTITIES_COUNT) {
        (resource.count() + 1).upto(limit) {
            assert resource.newInstance(getUniqueDummyConstructorArgs(it)).save()
        }
    }

    Map<String, Object> getUniqueDummyConstructorArgs(int counter) {
        [name: "$resourceName $counter"]
    }

    List<AbstractMarshallers> getMarshallers() { [] }

    abstract Class<T> getResource()

    // Following needs to be copied to subclasses. Grails mocking framework is not yet capable of handling such
    // level of abstraction

    /*

    @Unroll
    def "get outgoing relationships pagination: #no where max: #max offset: #offset"() {
        fixturesLoader.load('relationshipTypes/RT_relationship')
        RelationshipType relationshipType = fixturesLoader.RT_relationship.save() ?: RelationshipType.findByName('relationship')
        fillWithDummyEntities(15)

        expect:
        relationshipType

        when:

        def first = resource.get(1)

        first.outgoingRelationships = first.outgoingRelationships ?: []

        for (unit in resource.list()) {
            if (unit != first) {
                assert !Relationship.link(first, unit, relationshipType).hasErrors()
                if (first.outgoingRelationships.size() == 12) {
                    break
                }
            }
        }

        then:
        first.outgoingRelationships
        first.outgoingRelationships.size() == 12

        when:
        response.format = "json"
        params.offset = offset
        params.id = first.id

        controller.outgoing(max)
        JSONElement json = response.json


        recordResult "outgoing${no}", json

        then:

        json.success
        json.total == total
        json.size == size
        json.list
        json.list.size() == size
        json.next == next
        json.previous == previous

        cleanup:
        relationshipType?.delete()

        where:
        no | size | max | offset | total | next                                                       | previous
        1  | 10   | 10  | 0      | 12    | "/${resourceName}/outgoing/1?max=10&offset=10" | ""
        2  | 5    | 5   | 0      | 12    | "/${resourceName}/outgoing/1?max=5&offset=5"   | ""
        3  | 5    | 5   | 5      | 12    | "/${resourceName}/outgoing/1?max=5&offset=10"  | "/${resourceName}/outgoing/1?max=5&offset=0"
        4  | 4    | 4   | 8      | 12    | ""                                             | "/${resourceName}/outgoing/1?max=4&offset=4"
        5  | 2    | 10  | 10     | 12    | ""                                             | "/${resourceName}/outgoing/1?max=10&offset=0"
        6  | 2    | 2   | 10     | 12    | ""                                             | "/${resourceName}/outgoing/1?max=2&offset=8"
    }

    @Unroll
    def "get incoming relationships pagination: #no where max: #max offset: #offset"() {
        fixturesLoader.load('relationshipTypes/RT_relationship')
        RelationshipType relationshipType = fixturesLoader.RT_relationship.save() ?: RelationshipType.findByName('relationship')
        fillWithDummyEntities(15)

        expect:
        relationshipType

        when:
        def first = resource.get(1)
        first.incomingRelationships = first.incomingRelationships ?: []

        for (unit in  resource.list()) {
            if (unit != first) {
                assert !Relationship.link(unit, first, relationshipType).hasErrors()
                if (first.incomingRelationships.size() == 12) {
                    break
                }
            }
        }

        then:
        first.incomingRelationships
        first.incomingRelationships.size() == 12

        when:
        response.format = "json"
        params.offset = offset
        params.id = first.id

        controller.incoming(max)
        JSONElement json = response.json


        recordResult "incoming${no}", json

        then:

        json.success
        json.total == total
        json.size == size
        json.list
        json.list.size() == size
        json.next == next
        json.previous == previous

        cleanup:
        relationshipType?.delete()

        where:
        no | size | max | offset | total | next                                                       | previous
        1  | 10   | 10  | 0      | 12    | "/${resourceName}/incoming/1?max=10&offset=10" | ""
        2  | 5    | 5   | 0      | 12    | "/${resourceName}/incoming/1?max=5&offset=5"   | ""
        3  | 5    | 5   | 5      | 12    | "/${resourceName}/incoming/1?max=5&offset=10"  | "/${resourceName}/incoming/1?max=5&offset=0"
        4  | 4    | 4   | 8      | 12    | ""                                             | "/${resourceName}/incoming/1?max=4&offset=4"
        5  | 2    | 10  | 10     | 12    | ""                                             | "/${resourceName}/incoming/1?max=10&offset=0"
        6  | 2    | 2   | 10     | 12    | ""                                             | "/${resourceName}/incoming/1?max=2&offset=8"
    }


     */

}
