package com.gestionmorgue.benchmark;

import com.gestionmorgue.dao.GenericDao;
import com.gestionmorgue.model.Deceased;
import com.gestionmorgue.service.DeceasedService;
import com.gestionmorgue.util.DataInitializer;
import com.gestionmorgue.util.DatabaseManager;
import org.hibernate.Session;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.YearMonth;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 1, warmups = 0)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
public class HibernateQueriesBenchmark {

    private GenericDao<Deceased> dao;
    private DeceasedService service;

    @Setup
    public void setup() {
        DataInitializer.initialize();
        dao = new GenericDao<>(Deceased.class);
        service = new DeceasedService();
    }

    @TearDown
    public void tearDown() {
        DatabaseManager.shutdown();
    }

    @Benchmark
    public Object benchmarkFindPaginated() {
        return dao.findPaginated(0, 25);
    }

    @Benchmark
    public Object benchmarkSearchByQuery() {
        return service.search("Dupont", null, null);
    }

    @Benchmark
    public Object benchmarkFindRecent() {
        return service.getRecentDeceased(50);
    }

    @Benchmark
    public Object benchmarkCountByMonth() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            var cb = session.getCriteriaBuilder();
            var q = cb.createQuery(Long.class);
            var r = q.from(Deceased.class);
            YearMonth ym = YearMonth.now();
            q.select(cb.count(r)).where(
                cb.between(r.get("createdAt"),
                    ym.atDay(1).atStartOfDay(),
                    ym.atEndOfMonth().atTime(23, 59, 59)));
            return session.createQuery(q).getSingleResult();
        }
    }

    @Benchmark
    public Object benchmarkHqlAllDeceased() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            return session.createQuery("from Deceased d left join fetch d.interventions", Deceased.class)
                    .setMaxResults(100).list();
        }
    }

    public static void main(String[] args) throws Exception {
        var opt = new OptionsBuilder()
                .include(HibernateQueriesBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
