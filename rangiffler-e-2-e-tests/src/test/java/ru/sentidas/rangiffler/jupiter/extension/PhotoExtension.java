//package ru.sentidas.rangiffler.jupiter.extension;
//
//import guru.qa.niffler.jupiter.annotation.Spend;
//import guru.qa.niffler.jupiter.annotation.User;
//import guru.qa.niffler.model.spend.CategoryJson;
//import guru.qa.niffler.model.spend.SpendJson;
//import guru.qa.niffler.model.userdata.UserJson;
//import guru.qa.niffler.service.SpendClient;
//import guru.qa.niffler.service.impl.SpendDbClient;
//import org.apache.commons.lang3.ArrayUtils;
//import org.junit.jupiter.api.extension.*;
//import org.junit.platform.commons.support.AnnotationSupport;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//public class PhotoExtension implements BeforeEachCallback, ParameterResolver {
//
//    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(SpendingExtension.class);
//    private final PhotoClient spendClient = new PhotoDbClient();
//
//
//    @Override
//    public void beforeEach(ExtensionContext context) {
//        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
//                .ifPresent(anno -> {
//                    if (ArrayUtils.isNotEmpty(anno.spendings())) {
//                        UserJson createdUser = UserExtension.createdUser();
//                        final String username = createdUser != null
//                                ? createdUser.username()
//                                : anno.username();
//
//                        final List<SpendJson> createdSpendings = new ArrayList<>();
//
//                        for (Spend spendAnno : anno.spendings()) {
//                            SpendJson spendJson = new SpendJson(
//                                    null,
//                                    new Date(),
//                                    new CategoryJson(
//                                            null,
//                                            spendAnno.category(),
//                                            username,
//                                            false,
//                                            null
//                                    ),
//                                    spendAnno.currency(),
//                                    spendAnno.amount(),
//                                    spendAnno.description(),
//                                    username
//                            );
//
//                            createdSpendings.add(
//                                    spendClient.createSpend(spendJson));
//                        }
//
//                        if (createdUser != null) {
//                            createdUser.testData().spends().addAll(
//                                    createdSpendings
//                            );
//                        }
//                            context.getStore(NAMESPACE).put(
//                                    context.getUniqueId(),
//                                    createdSpendings);
//                    }
//                });
//    }
//
//    @Override
//    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
//        return parameterContext.getParameter().getType().isAssignableFrom(SpendJson[].class);
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public SpendJson[] resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
//        return (SpendJson[]) extensionContext.getStore(SpendingExtension.NAMESPACE)
//                .get(extensionContext.getUniqueId(), List.class)
//                .stream()
//                .toArray(SpendJson[]::new);
//    }
//}
