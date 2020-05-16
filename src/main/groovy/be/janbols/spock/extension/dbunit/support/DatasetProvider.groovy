package be.janbols.spock.extension.dbunit.support

import be.janbols.spock.extension.dbunit.DbUnit
import groovy.xml.MarkupBuilder
import org.dbunit.dataset.DataSetException
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.util.Nullable

/**
 *  Provides the xml data set from the {@link DbUnit} annotation or from an annotated field
 */
class DataSetProvider {

    private final DbUnit dbUnitAnnotation
    private final FieldInfo dataFieldInfo

    DataSetProvider(DbUnit dbUnitAnnotation, @Nullable FieldInfo dataFieldInfo) {
        this.dbUnitAnnotation = dbUnitAnnotation
        this.dataFieldInfo = dataFieldInfo
    }

    ReplacementDataSet findDataSet(Object target) {
        def dataSetClosure = dataFieldInfo?.readValue(target)
        if (!dataSetClosure && dbUnitAnnotation.content() && Closure.isAssignableFrom(dbUnitAnnotation.content())) {
            dataSetClosure = dbUnitAnnotation.content().newInstance(target, target)
        }

        String dataSetAsString = writeXmlDataSet(dataSetClosure as Closure)
        if (!dataSetAsString) {
            throw new ExtensionException("Failed to find a the data set. Specify one as a DbUnit-annotated field or provide one using @DbUnit.content")
        }

        return replacementDataSet(new StringReader(dataSetAsString))
    }

    private static String writeXmlDataSet(Closure dataSetClosure) {
        def xmlWriter = new StringWriter()
        def builder = new MarkupBuilder(xmlWriter)
        builder.dataset(dataSetClosure)
        return xmlWriter as String
    }

    private ReplacementDataSet replacementDataSet(Reader input) throws DataSetException {
        def flatXmlDataSet = new FlatXmlDataSetBuilder().setColumnSensing(dbUnitAnnotation.columnSensing()).build(input)

        def dataSet = new ReplacementDataSet(flatXmlDataSet)
        dataSet.addReplacementObject("[NULL]", null);
        dataSet.addReplacementObject("[NOW]", new Date());
        return dataSet;
    }


}
