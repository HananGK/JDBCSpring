package com.hanan.jdbc_spring;

import com.hanan.jdbc_spring.entidades.Cliente;
import com.hanan.jdbc_spring.mapeadores.ClienteMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


@SpringBootApplication
public class JdbcSpringApplication implements ApplicationRunner {
    @Autowired
    private JdbcTemplate jdbcTemp;

    private static final Logger log = LoggerFactory.getLogger(JdbcSpringApplication.class);

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public static void main(String[] args) {

        ConfigurableApplicationContext contexto = SpringApplication.run(JdbcSpringApplication.class, args);
        DataSource ds = contexto.getBean(DataSource.class);
        log.info("Implementación de DataSource -> " + ds.getClass().getSimpleName());

        //JdbcTemplate jdbcTemp = contexto.getBean(JdbcTemplate.class);

    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Integer ultCli = jdbcTemp.queryForObject("SELECT MAX(codigo_cliente) FROM cliente", Integer.class);
        log.info("El último código de cliente utilizado es: {}", ultCli);

//        String query = """
//                INSERT INTO oficina(codigo_oficina, ciudad, pais, region, codigo_postal, telefono, linea_direccion1)
//                VALUES(?, ?, ?, ?, ?, ?, ?)
//                """;
//
//        int filas = jdbcTemp.update(query, "AGP_ES", "Málaga", "España", "Andalucía", "12345", "123456789", "Calle Amapolas 5");
//        log.info("Se han añadido {} filas", filas);

        String query = "SELECT telefono FROM oficina WHERE codigo_oficina = :codOfi";
        SqlParameterSource parametros = new MapSqlParameterSource("codOfi", "AGP_ES");
        String ofi = namedParameterJdbcTemplate.queryForObject(query, parametros, String.class);
        log.info("El teléfono de la oficina es: {}", ofi);

        query = "SELECT * FROM cliente WHERE codigo_cliente = ?";
        Cliente cliente = jdbcTemp.queryForObject(query, new ClienteMap(), 25);
        assert  cliente!=null;
        log.info(cliente.toString());

        query = "SELECT * FROM cliente";
        List<Cliente> listaClientes = jdbcTemp.query(query, new ClienteMap());
        listaClientes.forEach(c -> log.info(c.toString()));

        String queryIns = "INSERT INTO Cliente(nombre_cliente) VALUES (?)";
        KeyHolder clavesGeneradas = new GeneratedKeyHolder();
//        int filas = jdbcTemp.update(query, "Carlos");
        int filas = jdbcTemp.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(queryIns, new String[]{"codigo_cliente"});
                ps.setString(1, "Carlos");
                return ps;
            }
        }, clavesGeneradas);
        System.out.println("Se ha añadido el cliente con código " + clavesGeneradas.getKey());
    }

}
