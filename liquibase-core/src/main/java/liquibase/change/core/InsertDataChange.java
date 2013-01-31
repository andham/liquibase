package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.InsertExecutablePreparedStatement;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Inserts data into an existing table.
 */
@DatabaseChange(name="insert", description = "Insert Row", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class InsertDataChange extends AbstractChange implements ChangeWithColumns<ColumnConfig> {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns;

    public InsertDataChange() {
        columns = new ArrayList<ColumnConfig>();
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustEqualExisting = "table")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustEqualExisting = "table.column")
    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        columns.remove(column);
    }

    public SqlStatement[] generateStatements(Database database) {

        boolean needsPreparedStatement = false;
        for (ColumnConfig column : columns) {
            if (column.getValueBlob() != null) {
                needsPreparedStatement = true;
            }
            if (column.getValueClob() != null) {
                needsPreparedStatement = true;
            }
//            if (column.getValueText() != null && database instanceof InformixDatabase) {
//                needsPreparedStatement = true;
//            }
        }

        if (needsPreparedStatement) {
            return new SqlStatement[] { 
            		new InsertExecutablePreparedStatement(database, catalogName, schemaName, tableName, columns)
            };
        }


        InsertStatement statement = new InsertStatement(getCatalogName(), getSchemaName(), getTableName());

        for (ColumnConfig column : columns) {

        	if (database.supportsAutoIncrement()
        			&& column.isAutoIncrement() != null && column.isAutoIncrement()) {
            	// skip auto increment columns as they will be generated by the database
            	continue;
            }

            statement.addColumnValue(column.getName(), column.getValueObject());
        }
        return new SqlStatement[]{
                statement
        };
    }

    /**
     * @see liquibase.change.Change#getConfirmationMessage()
     */
    public String getConfirmationMessage() {
        return "New row inserted into " + getTableName();
    }
}
