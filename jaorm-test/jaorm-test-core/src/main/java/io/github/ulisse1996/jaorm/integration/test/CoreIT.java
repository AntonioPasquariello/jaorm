package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.Sort;
import io.github.ulisse1996.jaorm.dsl.util.Pair;
import io.github.ulisse1996.jaorm.entity.EntityComparator;
import io.github.ulisse1996.jaorm.entity.Page;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.integration.test.entity.*;
import io.github.ulisse1996.jaorm.integration.test.query.*;
import io.github.ulisse1996.jaorm.spi.FeatureConfigurator;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("java:S100")
public abstract class CoreIT extends AbstractIT {

    private final UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);
    private final RoleDAO roleDAO = QueriesService.getInstance().getQuery(RoleDAO.class);
    private final UserRoleDAO userRoleDAO = QueriesService.getInstance().getQuery(UserRoleDAO.class);
    private final CityDAO cityDAO = QueriesService.getInstance().getQuery(CityDAO.class);
    private final SellerDao sellerDao = QueriesService.getInstance().getQuery(SellerDao.class);

    @Override
    protected void afterInit() throws Exception {
        execute("INSERT INTO USER_ENTITY(USER_ID, USER_NAME, DEPARTMENT_ID) VALUES (99, 'NAME_99', 99)");
    }

    // CRUD - Read

    @Test
    void should_read_user() {
        User user = getDefault();

        User found = userDAO.readByKey(99);
        assertSame(user, found);
        assertTotalInvocations(1);
    }

    @Test
    void should_read_opt_user() {
        User user = getDefault();

        Optional<User> opt = userDAO.readOptByKey(99);
        assertPresentAndSame(user, opt);
        assertTotalInvocations(1);
    }

    @Test
    void should_read_all_users() {
        User user = getDefault();

        List<User> list = userDAO.readAll();
        Assertions.assertFalse(list.isEmpty());
        assertSame(List.of(user), list);
        assertTotalInvocations(1);
    }

    @Test
    void should_read_page() {
        User user = getDefault();

        Page<User> page = userDAO.page(0, 10, Collections.singletonList(Sort.desc(UserColumns.USER_ID))); // 1 Only count
        Assertions.assertFalse(page.hasNext());
        Assertions.assertEquals(1, page.getCount());

        assertTotalInvocations(1);

        List<User> data = page.getData(); // 2
        Assertions.assertEquals(1, data.size());
        assertSame(user, data.get(0));

        assertTotalInvocations(2);
    }

    // CRUD - Update

    @Test
    void should_update_user() {
        User user = userDAO.readByKey(99); // 1
        user.setName("NEW_NAME");

        userDAO.update(user); // 2

        assertPresentAndSame(user, userDAO.readOptByKey(99)); // 3
        assertTotalInvocations(3);
    }

    @Test
    void should_update_with_batch() {
        Pair<User, User> users = createPair();

        userDAO.insert(List.of(users.getKey(), users.getValue())); // 2

        users.getKey().setName("NAME_11");
        users.getValue().setName("NAME_22");

        userDAO.updateWithBatch(List.of(users.getKey(), users.getValue())); // 3

        assertPresentAndSame(users.getKey(), userDAO.readOptByKey(1)); // 4
        assertPresentAndSame(users.getValue(), userDAO.readOptByKey(1)); // 5
        assertTotalInvocations(5);
    }

    // CRUD - Delete

    @Test
    void should_delete_user() {
        User user = userDAO.readByKey(99); // 1

        userDAO.delete(user); // 2

        Assertions.assertFalse(userDAO.readOptByKey(99).isPresent()); // 3
        assertTotalInvocations(3);
    }

    // CURD - Create

    @Test
    void should_insert_user() {
        User user = new User();
        user.setId(1);
        user.setName("NAME");

        userDAO.insert(user);

        Optional<User> opt = userDAO.readOptByKey(1);

        assertPresentAndSame(user, opt);
        assertTotalInvocations(2);
    }

    @Test
    void should_insert_with_batch() {
        Pair<User, User> users = createPair();

        userDAO.insertWithBatch(List.of(users.getKey(), users.getValue())); // 1

        assertPresentAndSame(users.getKey(), userDAO.readOptByKey(1)); // 2
        assertPresentAndSame(users.getValue(), userDAO.readOptByKey(1)); // 3

        assertTotalInvocations(3);
    }

    @Test
    public void should_insert_with_auto_generated() {
        AutoGenDao autoGenDao = QueriesService.getInstance().getQuery(AutoGenDao.class);

        AutoGenerated autoGenerated = new AutoGenerated();
        autoGenerated.setName("NAME_1");

        AutoGenerated autoGenerated2 = new AutoGenerated();
        autoGenerated2.setName("NAME_2");

        AutoGenerated autoGenerated3 = new AutoGenerated();
        autoGenerated3.setName("NAME_3");

        List<AutoGenerated> results = autoGenDao.insertWithBatch(List.of(autoGenerated, autoGenerated2, autoGenerated3)); // 1

        Assertions.assertNotNull(results);
        Assertions.assertFalse(results.isEmpty());
        Assertions.assertTrue(
                results.stream()
                        .map(AutoGenerated::getColGen)
                        .noneMatch(Objects::isNull)
        );

        assertTotalInvocations(1);
    }

    @Test
    public void should_return_all_generated_columns() {
        EntityWithProgressive p = new EntityWithProgressive();
        p.setValue("222");
        ProgressiveDao dao = QueriesService.getInstance().getQuery(ProgressiveDao.class);
        p = dao.insert(p); // 1

        Assertions.assertNotNull(p.getId());
        Assertions.assertEquals(BigDecimal.ONE, p.getId());
        Assertions.assertNotNull(p.getProgressive());
        Assertions.assertEquals(BigDecimal.valueOf(23), p.getProgressive());

        EntityWithProgressive pr = new EntityWithProgressive();
        pr.setId(p.getId());

        pr = dao.read(pr); // 2

        Assertions.assertTrue(EntityComparator.getInstance(EntityWithProgressive.class).equals(pr, p));

        assertTotalInvocations(2);
    }

    // CRUD - Upsert

    @Test
    void should_do_insert_for_missing_update() {
        User user = createPair().getKey();

        Assertions.assertTrue(FeatureConfigurator.getInstance().isInsertAfterFailedUpdateEnabled());

        userDAO.update(user); // 2 upsert

        assertPresentAndSame(user, userDAO.readOptByKey(1)); // 3

        assertTotalInvocations(3);
    }

    // Graphs

    @Test
    void should_get_full_user() {
        User user = createGraph();

        userDAO.insert(user); // 3 User, UserRole and UserSpecific

        Optional<User> result = User.USER_FULL.fetchOpt(createPair().getKey()); // 4

        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue(result.get().getUserSpecific().isPresent());
        Assertions.assertEquals(1, result.get().getRoles().size());
        assertSame(user, result.get());
        assertTotalInvocations(4);
    }

    //@Test Should be available when we implement sub graphs
    void should_get_full_user_with_roles() {
        User user = createGraph();
        Role role = new Role();
        role.setRoleName("NAME");
        role.setRoleId(1);
        user.getRoles().get(0).setRole(role);

        userDAO.insert(user); // 4 User, UserRole, Role and UserSpecific

        Optional<User> result = User.USER_FULL_WITH_ROLES.fetchOpt(createPair().getKey()); // 5

        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue(result.get().getUserSpecific().isPresent());
        Assertions.assertEquals(1, result.get().getRoles().size());
        Assertions.assertNotNull(result.get().getRoles().get(0).getRole());
        assertSame(user, result.get());
        assertTotalInvocations(5);
    }

    // Relationships
    @Test
    void should_delete_not_fetched_relationships() {
        User user = new User();
        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("NAME");
        Role role2 = new Role();
        role2.setRoleId(2);
        role2.setRoleName("NAME2");
        Role role3 = new Role();
        role3.setRoleId(3);
        role3.setRoleName("NAME3");

        user.setId(1);
        user.setName("NAME");
        user.setRoles(List.of(new UserRole(1, 1), new UserRole(1, 2), new UserRole(1, 3)));


        roleDAO.insert(List.of(role, role2, role3)); // 3 Roles
        userDAO.insert(user); // 7 User, UserRole (3)

        Optional<UserRole> optUserRole = userRoleDAO.readOptByKeys(1, 1); // 8
        Optional<UserRole> optUserRole2 = userRoleDAO.readOptByKeys(2, 1); // 9
        Optional<UserRole> optUserRole3 = userRoleDAO.readOptByKeys(3, 1); // 10

        Assertions.assertTrue(optUserRole.isPresent());
        Assertions.assertTrue(optUserRole2.isPresent());
        Assertions.assertTrue(optUserRole3.isPresent());

        User u = userDAO.readByKey(1); // 11

        userDAO.delete(u); // 14 Delete UserRole (1 query for 3 delete), Delete UserSpecific, Delete User

        optUserRole = userRoleDAO.readOptByKeys(1, 1); // 15
        optUserRole2 = userRoleDAO.readOptByKeys(2, 1); // 16
        optUserRole3 = userRoleDAO.readOptByKeys(3, 1); // 17

        Assertions.assertFalse(optUserRole.isPresent());
        Assertions.assertFalse(optUserRole2.isPresent());
        Assertions.assertFalse(optUserRole3.isPresent());
        assertTotalInvocations(17);
    }

    @Test
    void should_delete_not_fetched_relationships_recursively() {
        Store store = new Store(1, "NAME", 1);
        store.setSellers(List.of(new Seller(1, "SELLER1", 1), new Seller(2, "SELLER2", 1)));
        Store store2 = new Store(2, "NAME2", 1);
        store2.setSellers(List.of(new Seller(3, "SELLER1", 2), new Seller(4, "SELLER2", 2)));

        City city = new City();
        city.setCityId(1);
        city.setName("NAME");
        city.setStores(List.of(store, store2));

        cityDAO.insert(city); // 7 City, Store (2), Seller (4)

        Assertions.assertEquals(4, sellerDao.readAll().size()); // 8

        cityDAO.deleteByKey(1); // 11 City, Store (1), Seller (1)

        Assertions.assertEquals(0, sellerDao.readAll().size()); // 12
        assertTotalInvocations(12);
    }

    // Utils

    @NotNull
    private User createGraph() {
        User user = createPair().getKey();
        UserRole role = new UserRole();
        role.setRoleId(1);
        role.setUserId(1);
        UserSpecific specific = new UserSpecific();
        specific.setSpecificId(1);
        specific.setUserId(1);
        user.setUserSpecific(Result.of(specific));
        user.setRoles(Collections.singletonList(role));
        return user;
    }

    private Pair<User, User> createPair() {
        User user = new User();
        user.setId(1);
        user.setName("NAME_1");

        User user2 = new User();
        user2.setId(2);
        user2.setName("NAME_2");

        return new Pair<>(user, user2);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void assertPresentAndSame(User user, Optional<User> opt) {
        Assertions.assertTrue(opt.isPresent());
        assertSame(user, opt.get());
    }

    private void assertSame(List<User> users, List<User> others) {
        EntityComparator.getInstance(User.class)
                .equals(users, others);
    }

    private void assertSame(User user, User other) {
        EntityComparator.getInstance(User.class)
                .equals(user, other);
    }

    @NotNull
    private User getDefault() {
        User user = new User();
        user.setId(99);
        user.setName("NAME_99");
        user.setDepartmentId(99);
        return user;
    }
}
