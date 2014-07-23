<?xml version="1.0" encoding="UTF-8"?>
<!--
This transformation maps a catalogue definition into a hibernate mapping file. Two database mappings are provided,
the online mapping which maps dimensions and hierarchies to normalized tables and the warehouse mapping which maps fact
tables and associated dimensions and hierarchies to a denormalized 'star schema' form.

Global parameters that must be passed to this template:
package  The output package name.
-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cat="http://thesett.com/catalogue-def-0.1"
                xmlns:gen="http://thesett.com/source-code-generator-0.1"
                xmlns:xalan="http://xml.apache.org/xalan"
                extension-element-prefixes="gen">

    <xsl:output method="xml" 
                version="1.0" 
                encoding="UTF-8" 
                indent="yes"
                xalan:indent-amount="2"
                doctype-public="-//Hibernate/Hibernate Mapping DTD 3.0//EN"
                doctype-system="http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd"/>

    <xalan:component prefix="gen" elements="" functions="toCamelCase">
        <xalan:script lang="javaclass" src="xalan://com.thesett.common.util.StringUtils"/>
    </xalan:component>

    <!-- The caller should pass the output package name in this parameter. -->
    <xsl:param name="package"/>

    <!-- Match the root of the document. -->
    <xsl:template match="/">

        <xsl:element name="hibernate-mapping">
            <xsl:attribute name="package"><xsl:value-of select="$package"/></xsl:attribute>
            
            <!-- Generate typedefs for all the hierarchy types. -->
            <xsl:for-each select="cat:CatalogueDefinition/cat:HierarchyDef">
                <xsl:call-template name="hierarchy_typedef"/>
            </xsl:for-each>

            <!-- Generate persistence mappings for all the hierarchy types. -->
            <xsl:for-each select="cat:CatalogueDefinition/cat:HierarchyDef">
                <xsl:call-template name="hierarchy_persistence"/>
            </xsl:for-each>
            
            <!-- Match each of the entity definitions for mapping to the online system. -->
            <xsl:for-each select="cat:CatalogueDefinition/cat:EntityDef|cat:CatalogueDefinition/cat:DimensionDef">
                <xsl:call-template name="online_entity"/>
            </xsl:for-each>

            <!-- Match all of the component, view and entity definitions for mapping to the warehouse as well as dimensions. -->
            <!-- In practice not all component, view and entities need to be mapped, only those that are actually used in
                 the warehouse. This will come with the intelligent code generator. -->
            <!-- Note that a seperate call is made for the views as their implementation classes are named differently. -->
            <xsl:for-each select="cat:CatalogueDefinition/cat:ComponentDef|cat:CatalogueDefinition/cat:EntityDef|cat:CatalogueDefinition/cat:DimensionDef">
                <xsl:call-template name="warehouse_entity">
                    <xsl:with-param name="classname" select="gen:toCamelCaseUpper(@name)"/>
                </xsl:call-template>
            </xsl:for-each>

            <xsl:for-each select="cat:CatalogueDefinition/cat:ViewDef">
                <xsl:call-template name="warehouse_entity">
                    <xsl:with-param name="classname" select="gen:toCamelCaseUpper(@name)"/>
                </xsl:call-template>
            </xsl:for-each>

            <!-- Match the fact table definitions and map to tables with many-to-one links to associated dimensions. -->
            <xsl:for-each select="cat:CatalogueDefinition/cat:FactDef">
                <xsl:call-template name="facttable"/>
            </xsl:for-each>

            <!-- Specify the mapping for external id's. -->
            <xsl:element name="class">
                <xsl:attribute name="name">com.thesett.catalogue.interfaces.ExternalId</xsl:attribute>
                <xsl:attribute name="table"><xsl:value-of select="@name"/>external_id</xsl:attribute>

                <!-- Create a UUID identifier for the external id. -->
                <xsl:element name="id">
                    <xsl:attribute name="name">id</xsl:attribute>
                    <xsl:attribute name="type">string</xsl:attribute>

                    <xsl:element name="column">
                        <xsl:attribute name="name">id</xsl:attribute>
                        <xsl:attribute name="sql-type">char(32)</xsl:attribute>
                        <xsl:attribute name="not-null">true</xsl:attribute>
                    </xsl:element>

                    <xsl:element name="generator">
                        <xsl:attribute name="class">uuid.hex</xsl:attribute>
                    </xsl:element>

                </xsl:element>

                <!-- The resource class column. -->
                <xsl:element name="property">
                    <xsl:attribute name="name">resource</xsl:attribute>
                    <xsl:attribute name="type">string</xsl:attribute>
                </xsl:element>

            </xsl:element>

        </xsl:element>
        
    </xsl:template>

    <!-- Match the hierarchy definitions and transform them into typedef declarations. -->
    <xsl:template name="hierarchy_typedef" match="cat:HierarchyDef">

        <!--
            Each hierarchy has its own user type defined, ensure the first letter of the type name is capitalized to form
            the class name for this user type.
        -->
        <xsl:variable name="classname" select="gen:toCamelCaseUpper(@name)"/>

        <xsl:element name="typedef">
            <xsl:attribute name="class"><xsl:value-of select="$package"/>.<xsl:value-of select="$classname"/>UserType</xsl:attribute>
            <xsl:attribute name="name"><xsl:value-of select="@name"/>_hierarchy</xsl:attribute>
        </xsl:element>

    </xsl:template>

    <!-- Match the hierarchy definitions and transform them into persistence mappings as entities in their own right. -->
    <xsl:template name="hierarchy_persistence" match="cat:HierarchyDef">

        <!-- Ensure that the first letter of the entity class name is capitalized. -->
        <xsl:variable name="classname" select="gen:toCamelCaseUpper(@name)"/>

        <xsl:element name="class">
            <xsl:attribute name="name"><xsl:value-of select="$classname"/></xsl:attribute>
            <xsl:attribute name="table"><xsl:value-of select="@name"/>_hierarchy</xsl:attribute>

            <!-- Create a long integer identifier. -->
            <xsl:element name="id">

                <xsl:attribute name="name">id</xsl:attribute>
                <xsl:attribute name="column">id</xsl:attribute>
                <xsl:attribute name="type">long</xsl:attribute>

                <!-- Use the 'assigned' id generator type as hierarchy attributes generate their own ids. -->
                <xsl:element name="generator">
                    <xsl:attribute name="class">assigned</xsl:attribute>
                </xsl:element>

            </xsl:element>

            <!-- Map hierarchy levels to columns. -->
            <xsl:element name="property">
                <xsl:attribute name="name"><xsl:value-of select="gen:toCamelCase(@name)"/></xsl:attribute>
                <xsl:attribute name="type"><xsl:value-of select="@name"/>_hierarchy</xsl:attribute>

                <xsl:apply-templates select="cat:Level"/>
            </xsl:element>

        </xsl:element>

    </xsl:template>

    <!-- Match hierarchy level names and map them to database columns. -->
    <xsl:template match="cat:Level">

        <xsl:element name="column">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
        </xsl:element>

        <!-- Match further levels. -->
        <xsl:apply-templates select="cat:Level"/>

    </xsl:template>

    <!-- Match dimension definitions and transform them to a mapping to a normalized online database. -->
    <xsl:template name="online_entity" match="cat:DimensionDef|cat:EntityDef">

        <!-- Ensure that the first letter of the class name is capitalized. -->
        <xsl:variable name="classname" select="gen:toCamelCaseUpper(@name)"/>

        <xsl:element name="class">
            <xsl:attribute name="name"><xsl:value-of select="$classname"/></xsl:attribute>
            <xsl:attribute name="table"><xsl:value-of select="@name"/>_online</xsl:attribute>
            <xsl:attribute name="entity-name"><xsl:value-of select="@name"/>_Online</xsl:attribute>

            <!-- Create a long integer identifier. All dimensions use this. -->
            <xsl:element name="id">

                <xsl:attribute name="name">id</xsl:attribute>
                <xsl:attribute name="column">id</xsl:attribute>
                <xsl:attribute name="type">long</xsl:attribute>

                <xsl:element name="generator">
                    <xsl:attribute name="class">native</xsl:attribute>
                </xsl:element>

            </xsl:element>

            <!-- Match each of the dimensions basic properties, and map as properties. -->
            <xsl:apply-templates select="cat:String|cat:Integer|cat:Real|cat:Time|cat:Date"/>

            <!-- Match each of the entities simple references to other entities, and map as many-to-ones. -->
            <xsl:for-each select="cat:Component">
                <xsl:call-template name="linked_references"/>
            </xsl:for-each>

            <!-- Ensure that the external id column is mapped. -->
            <xsl:element name="many-to-one">
                <xsl:attribute name="name">externalId</xsl:attribute>
                <xsl:attribute name="column">externalId</xsl:attribute>
                <xsl:attribute name="not-null">true</xsl:attribute>
                <xsl:attribute name="lazy">false</xsl:attribute>
                <xsl:attribute name="unique">true</xsl:attribute>
            </xsl:element>

            <!-- Match hierarchies and map them as many-to-one links to hierarchies as seperate entities. -->
            <xsl:for-each select="cat:Hierarchy">
                <xsl:call-template name="hierarchy_entity"/>
            </xsl:for-each>

        </xsl:element>

    </xsl:template>

    <!-- Match the dimension definitions and transform them to a mapping to a denormalized warehouse database. -->
    <xsl:template name="warehouse_entity"> 
        <xsl:param name="classname"/>

        <xsl:element name="class">
            <xsl:attribute name="name"><xsl:value-of select="$classname"/></xsl:attribute>
            <xsl:attribute name="table"><xsl:value-of select="@name"/>_warehouse</xsl:attribute>
            <xsl:attribute name="entity-name"><xsl:value-of select="@name"/>_Warehouse</xsl:attribute>

            <!-- Create a long integer identifier. All dimensions use this. -->
            <xsl:element name="id">

                <xsl:attribute name="name">id</xsl:attribute>
                <xsl:attribute name="column">id</xsl:attribute>
                <xsl:attribute name="type">long</xsl:attribute>

                <xsl:element name="generator">
                    <xsl:attribute name="class">native</xsl:attribute>
                </xsl:element>

            </xsl:element>

            <!-- Match each of the dimensions properties and map as properties. -->
            <xsl:apply-templates select="cat:String|cat:Integer|cat:Real|cat:Time|cat:Date"/>

            <!-- Match hierarchies and map them as embeded components. -->
            <xsl:for-each select="cat:Hierarchy">
                <xsl:call-template name="hierarchy_component"/>
            </xsl:for-each>

        </xsl:element>

    </xsl:template>

    <!-- Transforms string attributes to property declarations. -->
    <xsl:template match="cat:String">
        <xsl:element name="property">
            <xsl:attribute name="name"><xsl:value-of select="gen:toCamelCase(@name)"/></xsl:attribute>
            <xsl:attribute name="column"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="type">text</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <!-- Transforms int attributes to property declarations. -->
    <xsl:template match="cat:Integer">
        <xsl:element name="property">
            <xsl:attribute name="name"><xsl:value-of select="gen:toCamelCase(@name)"/></xsl:attribute>
            <xsl:attribute name="column"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="type">int</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <!-- Transforms real attributes to property declarations. -->
    <xsl:template match="cat:Real">
        <xsl:element name="property">
            <xsl:attribute name="name"><xsl:value-of select="gen:toCamelCase(@name)"/></xsl:attribute>
            <xsl:attribute name="column"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="type">float</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <!-- Transforms time attributes to property declarations. -->
    <xsl:template match="cat:Time">
        <xsl:element name="property">
            <xsl:attribute name="name"><xsl:value-of select="gen:toCamelCase(@name)"/></xsl:attribute>
            <xsl:attribute name="column"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="type">long</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <!-- Transforms date attributes to property declarations. -->
    <xsl:template match="cat:Date">
        <xsl:element name="property">
            <xsl:attribute name="name"><xsl:value-of select="gen:toCamelCase(@name)"/></xsl:attribute>
            <xsl:attribute name="column"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="type">date</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <!-- Transforms component attributes ordinary many-to-one foreign key to primary key references. -->
    <xsl:template name="linked_references" match="cat:Component">

        <!-- Look up the component that is referenced, and map it as a component, or an entity
             depending on its type. -->
        <xsl:variable name="refname" select="@type"/>
        <xsl:variable name="propname" select="gen:toCamelCase(@name)"/>

        <xsl:for-each select="//cat:CatalogueDefinition/cat:ComponentDef[@name = $refname]">
            <xsl:call-template name="linked_components">
                <xsl:with-param name="property_name" select="$propname"/>
            </xsl:call-template>
        </xsl:for-each>

        <xsl:for-each select="//cat:CatalogueDefinition/cat:EntityDef[@name = $refname]|//cat:CatalogueDefinition/cat:DimensionDef[@name = $refname]">
            <xsl:call-template name="linked_entities">
                <xsl:with-param name="property_name" select="$propname"/>
            </xsl:call-template>
        </xsl:for-each>

    </xsl:template>

    <xsl:template name="linked_components">
        <xsl:param name="property_name"/>

        <xsl:element name="component">
            <xsl:attribute name="name"><xsl:value-of select="$property_name"/></xsl:attribute>

            <!-- Match each of the components basic properties, and map as properties. -->
            <xsl:apply-templates select="cat:String|cat:Integer|cat:Real|cat:Time|cat:Date"/>

            <!-- Match each of the components simple references to other components. -->
            <xsl:for-each select="cat:Component">
                <xsl:call-template name="linked_references"/>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template name="linked_entities">
        <xsl:param name="property_name"/>

        <xsl:element name="many-to-one">
            <xsl:attribute name="name"><xsl:value-of select="$property_name"/></xsl:attribute>
            <xsl:attribute name="column"><xsl:value-of select="$property_name"/>_id</xsl:attribute>
            <xsl:attribute name="entity-name"><xsl:value-of select="@name"/>_Online</xsl:attribute>
            <xsl:attribute name="not-null">false</xsl:attribute>
            <xsl:attribute name="lazy">proxy</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <!-- Generate many-to-one mappings to hierarchy tables with hierarchies modelled as entities. -->
    <xsl:template name="hierarchy_entity" match="cat:Hierarchy">
        <xsl:element name="many-to-one">
            <xsl:attribute name="name"><xsl:value-of select="gen:toCamelCase(@name)"/></xsl:attribute>
            <xsl:attribute name="column"><xsl:value-of select="@name"/>_id</xsl:attribute>
            <xsl:attribute name="not-null">true</xsl:attribute>
            <xsl:attribute name="lazy">false</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <!-- Generate hierarchy mappings as embeded components. -->
    <xsl:template name="hierarchy_component" match="cat:Hierarchy">

        <!-- 
             Extract the component class name from the type and ensure that the first letter of the class name is
             capitalized.
        -->
        <xsl:variable name="classname" select="gen:toCamelCaseUpper(@type)"/>

        <xsl:element name="component">
            <xsl:attribute name="name"><xsl:value-of select="gen:toCamelCase(@name)"/></xsl:attribute>
            <xsl:attribute name="class"><xsl:value-of select="$classname"/></xsl:attribute>

            <xsl:element name="property">
                <xsl:attribute name="name"><xsl:value-of select="gen:toCamelCase(@type)"/></xsl:attribute>
                <xsl:attribute name="type"><xsl:value-of select="@type"/>_hierarchy</xsl:attribute>
                
                <!-- Column mappings from the associated hierarchy type ref. -->
                <xsl:variable name="refname" select="@type"/>
                <xsl:apply-templates select="//cat:CatalogueDefinition/cat:HierarchyDef[@name = $refname]/cat:Level"/>

            </xsl:element>
        </xsl:element>

    </xsl:template>

    <!-- Generate facttable mappings to tables with many-to-one links to associated dimensions. -->
    <xsl:template name="facttable" match="cat:FactDef">

        <!-- Ensure the first letter of the class name is capitalized. -->
        <xsl:variable name="classname" select="gen:toCamelCaseUpper(@name)"/>

        <xsl:element name="class">
            <xsl:attribute name="name"><xsl:value-of select="$classname"/></xsl:attribute>
            <xsl:attribute name="table"><xsl:value-of select="@name"/>_facttable</xsl:attribute>

            <!-- Create a long integer identifier. -->
            <xsl:element name="id">

                <xsl:attribute name="name">id</xsl:attribute>
                <xsl:attribute name="column">id</xsl:attribute>
                <xsl:attribute name="type">long</xsl:attribute>

                <xsl:element name="generator">
                    <xsl:attribute name="class">native</xsl:attribute>
                </xsl:element>

            </xsl:element>

            <!-- Map any declared attributes to properties. -->
            <xsl:apply-templates select="cat:String|cat:Integer|cat:Real"/>

            <!-- Map any built in auto dimensions as many-to-one links to their pre-defined tables and classes. -->
            <xsl:apply-templates select="cat:Date|cat:Time"/>

            <!-- Map any linked dimensions as many-to-one links to the denormalized warehouse dimension tables. -->
            <xsl:for-each select="cat:Component">
                <xsl:call-template name="linked_warehouse_components"/>
            </xsl:for-each>

        </xsl:element>

    </xsl:template>

    <!-- Generate many-to-one mappings to denormalized warehouse dimension tables. -->
    <xsl:template name="linked_warehouse_components" match="cat:Component">
        <xsl:element name="many-to-one">
            <xsl:attribute name="name"><xsl:value-of select="gen:toCamelCase(@name)"/></xsl:attribute>
            <xsl:attribute name="column"><xsl:value-of select="@name"/>_id</xsl:attribute>
            <xsl:attribute name="entity-name"><xsl:value-of select="@type"/>_Warehouse</xsl:attribute>
            <xsl:attribute name="not-null">true</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <!-- Generate many-to-one mappings to the date dimension warehouse table. -->
    <!--<xsl:template match="cat:Date">
        <xsl:element name="many-to-one">
        <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
        <xsl:attribute name="column"><xsl:value-of select="@name"/>_id</xsl:attribute>
        <xsl:attribute name="not-null">true</xsl:attribute>
        </xsl:element>
        </xsl:template>-->

    <!-- Generate many-to-one mappings to the time-of-day dimension warehouse table. -->
    <!--<xsl:template match="cat:Time">
        <xsl:element name="many-to-one">
        <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
        <xsl:attribute name="column"><xsl:value-of select="@name"/>_id</xsl:attribute>
        <xsl:attribute name="not-null">true</xsl:attribute>
        </xsl:element>
        </xsl:template>-->

</xsl:stylesheet>

