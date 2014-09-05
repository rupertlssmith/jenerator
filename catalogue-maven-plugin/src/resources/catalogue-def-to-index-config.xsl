<?xml version="1.0" encoding="UTF-8"?>
<!-- This transformation maps a catalogue definition into an index configuration file. -->

<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cat="http://thesett.com/catalogue-def-0.2"
                xmlns:gen="com.thesett.common.util.StringUtils"
                xmlns="http://thesett.com/index-setup-0.1"
                xmlns:xalan="http://xml.apache.org/xalan"
                extension-element-prefixes="gen">

    <xsl:output method="xml" 
                version="1.0" 
                encoding="UTF-8" 
                indent="yes"
                xalan:indent-amount="2"/>

    <xsl:script implements-prefix="gen" language="java" src="java:com.thesett.common.util.StringUtils"/>

    <!-- The caller should pass the output package name in this parameter. -->
    <xsl:param name="package"/>

    <!-- Match the root of the document. -->
    <xsl:template match="/">

        <index-configurations>

            <!-- Generate configurations for all specified indexes. -->
            <xsl:for-each select="cat:CatalogueDefinition/cat:Index">
                <xsl:call-template name="index_def"/>
            </xsl:for-each>    

        </index-configurations>

    </xsl:template>

    <xsl:template name="index_def" match="cat:Index">

        <index-configuration name="{@name}">
            <key-base-class name="com.thesett.catalogue.model.ExternalId"/>
            <record-base-class name="com.thesett.catalogue.model.ComponentInstance"/>
            <summary-base-class name="com.thesett.catalogue.model.ViewInstance"/>
       
            <xsl:for-each select="cat:IndexedComponent">
                <xsl:call-template name="dimension_mapping"/>
            </xsl:for-each> 

        </index-configuration>
    </xsl:template>

    <xsl:template name="dimension_mapping" match="cat:IndexedComponent">
        <mapping>

            <!-- Work out the class name of the dimensions operation level implementation. -->
            <xsl:variable name="stringUtils" select="gen:new()"/>
            <xsl:variable name="classname" select="gen:toCamelCaseUpperFunc($stringUtils, string(@type))"/>
            <xsl:variable name="summary_classname" select="gen:toCamelCaseUpperFunc($stringUtils, cat:SummaryView/@type)"/>
            <xsl:variable name="rating_field" select="gen:toCamelCaseFunc($stringUtils, cat:RelevanceValue/@name)"/>

            <record-class name="{$package}.{$classname}">

                <!-- Match and generate each of the fields to be indexed. -->
                <xsl:for-each select="cat:IndexBy">
                    <xsl:call-template name="field_mapping"/>
                </xsl:for-each>

            </record-class>

            <summary-class name="{$package}.{$summary_classname}Impl">
                <rating-field name="{$rating_field}"/>
            </summary-class>

        </mapping>
    </xsl:template>

    <xsl:template name="field_mapping" match="cat:IndexBy">
        <xsl:variable name="stringUtils" select="gen:new()"/>
        <xsl:variable name="field_name" select="gen:toCamelCaseFunc($stringUtils, @name)"/>

        <field name="{$field_name}"/>
    </xsl:template>

</xsl:stylesheet>
