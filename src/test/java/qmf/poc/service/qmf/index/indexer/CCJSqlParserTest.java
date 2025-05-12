package qmf.poc.service.qmf.index.indexer;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.delete.ParenthesedDelete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CCJSqlParserTest {
    @Test
    public void testCCJSqlParser() throws ParseException {
        // Arrange
        final String sql = "SELECT coulmn2, sum(total) FROM table WHERE column = 'value'";
        // Act
        final CCJSqlParser parser = new CCJSqlParser(sql);
        Statements statements = parser.withErrorRecovery().Statements();
        assertEquals(1, statements.size());
    }

    @Test
    public void testCCJSqlParserDetectFields() throws ParseException {
        // Arrange
        final String sql = "SELECT coulmn2, sum(total) FROM table WHERE column = 'value'";
        // Act
        final CCJSqlParser parser = new CCJSqlParser(sql);
        Statements statements = parser.withErrorRecovery().Statements();
        Statement statement = statements.get(0);
        statement.accept(new StatementVisitor<Object>() {
            @Override
            public <S> Object visit(Analyze analyze, S s) {
                return null;
            }

            @Override
            public <S> Object visit(SavepointStatement savepointStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(RollbackStatement rollbackStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Comment comment, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Commit commit, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Delete delete, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Update update, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Insert insert, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Drop drop, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Truncate truncate, S s) {
                return null;
            }

            @Override
            public <S> Object visit(CreateIndex createIndex, S s) {
                return null;
            }

            @Override
            public <S> Object visit(CreateSchema createSchema, S s) {
                return null;
            }

            @Override
            public <S> Object visit(CreateTable createTable, S s) {
                return null;
            }

            @Override
            public <S> Object visit(CreateView createView, S s) {
                return null;
            }

            @Override
            public <S> Object visit(AlterView alterView, S s) {
                return null;
            }

            @Override
            public <S> Object visit(RefreshMaterializedViewStatement refreshMaterializedViewStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Alter alter, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Statements statements, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Execute execute, S s) {
                return null;
            }

            @Override
            public <S> Object visit(SetStatement setStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(ResetStatement resetStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(ShowColumnsStatement showColumnsStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(ShowIndexStatement showIndexStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(ShowTablesStatement showTablesStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Merge merge, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Select select, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Upsert upsert, S s) {
                return null;
            }

            @Override
            public <S> Object visit(UseStatement useStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Block block, S s) {
                return null;
            }

            @Override
            public <S> Object visit(DescribeStatement describeStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(ExplainStatement explainStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(ShowStatement showStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(DeclareStatement declareStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(Grant grant, S s) {
                return null;
            }

            @Override
            public <S> Object visit(CreateSequence createSequence, S s) {
                return null;
            }

            @Override
            public <S> Object visit(AlterSequence alterSequence, S s) {
                return null;
            }

            @Override
            public <S> Object visit(CreateFunctionalStatement createFunctionalStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(CreateSynonym createSynonym, S s) {
                return null;
            }

            @Override
            public <S> Object visit(AlterSession alterSession, S s) {
                return null;
            }

            @Override
            public <S> Object visit(IfElseStatement ifElseStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(RenameTableStatement renameTableStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(PurgeStatement purgeStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(AlterSystemStatement alterSystemStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(UnsupportedStatement unsupportedStatement, S s) {
                return null;
            }

            @Override
            public <S> Object visit(ParenthesedInsert parenthesedInsert, S s) {
                return null;
            }

            @Override
            public <S> Object visit(ParenthesedUpdate parenthesedUpdate, S s) {
                return null;
            }

            @Override
            public <S> Object visit(ParenthesedDelete parenthesedDelete, S s) {
                return null;
            }
        });
    }
}
