package idCardCv;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;

import domain.TCzrk;
import oracle.jdbc.pool.OracleDataSource;

public class JsqlBoxTest {
	
	public static void main(String[] args) throws Exception {
		DbContext ctx = new DbContext(new GongAnDataSource().create());
		ctx.setAllowShowSQL(true); //开启SQL日志输出
		DbContext.setGlobalDbContext(ctx); //设定全局DbContext
		List<TCzrk> list = ctx.iQueryForEntityList(TCzrk.class,"select * from T_CZRK t where GMSFHM=?",DB.param("150404198812032611"));
		System.out.println(list);
	}
	
	public static class GongAnDataSource{
	    public DataSource create() throws SQLException {
	        OracleDataSource ds = new OracleDataSource();
	        ds.setURL("jdbc:oracle:thin:@192.168.0.182:1521:orcl");
	        ds.setUser("practice");
	        ds.setPassword("practice");
	        return ds;
	    }
	}
}
