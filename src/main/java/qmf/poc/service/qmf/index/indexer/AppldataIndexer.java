package qmf.poc.service.qmf.index.indexer;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
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
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppldataIndexer {
    public static List<String> index(String type, String appldata) {
        return index(type, AppldataParser.parse(appldata));
    }

    public static List<String> index(String type, AppldataParseResult parsedAppldata) {
        //noinspection EnhancedSwitchMigration
        switch (type) {
            case "QUERY":
                return indexSQL(parsedAppldata.body);
            case "PROC":
                return indexProc(parsedAppldata.body);
            case "FORM":
            default:
                return new LinkedList<>();
        }
    }

    static List<String> indexSQL(List<String> statements) {
        final List<String> result = new LinkedList<>();
        for (String statement : statements) {
            result.addAll(indexSQL(statement));
        }
        return result;
    }

    static List<String> indexSQL(String statement) {
        final Set<String> result = new HashSet<>();
        try {
            result.addAll(TablesNamesFinder.findTables(statement));
        } catch (JSQLParserException ignored) {
            // Ignore exception
        }
        try {
            result.addAll(TablesNamesFinder.findTablesInExpression(statement));
        } catch (JSQLParserException ignored) {
            // Ignore exception
        }
        try {
            result.addAll(TablesNamesFinder.findTablesOrOtherSources(statement));
        } catch (JSQLParserException ignored) {
            // Ignore exception
        }
        try {
            CCJSqlParser parser = new CCJSqlParser(statement);
            Statements statements = parser.withErrorRecovery().Statements();
            for (Statement stmt : statements) {
                if (stmt == null) {
                    continue;
                }
                stmt.accept(new StatementVisitor<>() {
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
                        if (select instanceof PlainSelect plainSelect) {
                            List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
                            for (SelectItem<?> item : selectItems) {
                                if (item == null) {
                                    continue;
                                }
                                final Alias alias = item.getAlias();
                                if (alias != null) {
                                    final String name = alias.getUnquotedName();
                                    if (name != null) {
                                        result.add(name);
                                    }
                                }
                                final Object expression = item.getExpression();
                                if (expression != null) {
                                    final String name =
                                            (expression instanceof Column)
                                                    ? ((Column) expression).getColumnName()
                                                    : expression.toString();
                                    if (name != null) {
                                        result.add(name);
                                    }
                                }
                            }
                        }
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
        } catch (ParseException ignored) {
            // Ignore exception
        }
        return new ArrayList<>(result);
    }

    static List<String> indexProc(List<String> body) {
        final List<String> result = new LinkedList<>();
        for (String proc : body) {
            result.addAll(indexProc(proc));
        }
        return result;
    }

    private static final String regex1 = "(?:^|[^a-zA-Z])RUN(?:\\s+QUERY)?\\s+([a-zA-Z0-9_.]+)";
    private static final String regex2 = "(?:^|[^a-zA-Z])RUN(?:\\s+PROC)?\\s+([a-zA-Z0-9_.]+)";
    private static final List<Pattern> patterns = List.of(
            Pattern.compile(regex1),
            Pattern.compile(regex2)
    );

    private static final Set<String> ignored = Set.of(
            "QUERY",
            "PROC"
    );


    static List<String> indexProc(String proc) {
        final Set<String> result = new HashSet<>();
        for (Pattern pattern : patterns) {
            final Matcher matcher = pattern.matcher(proc);
            while (matcher.find()) {
                final String match = matcher.group(1).trim();
                if (ignored.contains(match)) {
                    continue;
                }
                result.add(match);
            }
        }
        return new ArrayList<>(result);
    }
}
