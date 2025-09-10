//package ru.sentidas.rangiffler.service.utils;
//
//import ru.sentidas.rangiffler.data.entity.UserEntity;
//
//public class ProtoMapper {
//    private UserServerProtoMapper() {}
//
//    public static ru.sentidas.rangiffler.grpc.UserResponse toProto(UserService.UserWithComputedStatus uw) {
//        return toProto(uw.user(), uw.status());
//    }
//
//    public static ru.sentidas.rangiffler.grpc.UserResponse toProto(UserEntity u, UserService.FriendStatus status) {
//        var b = ru.sentidas.rangiffler.grpc.UserResponse.newBuilder()
//                .setId(u.getId().toString())
//                .setUsername(u.getUsername())
//                .setCountryCode(u.getCountryCode());
//
//        if (u.getFirstname() != null) b.setFirstname(u.getFirstname());
//        if (u.getSurname()   != null) b.setSurname(u.getSurname());
//        if (u.getAvatar()    != null && u.getAvatar().length > 0) {
//            // как отдаёшь avatar — реши сам (base64 или dataURL),
//            // ниже — просто base64 строка
//            b.setAvatar(java.util.Base64.getEncoder().encodeToString(u.getAvatar()));
//        }
//        if (status != null) {
//            b.setFriendStatus(ru.sentidas.rangiffler.grpc.FriendStatus.valueOf(status.name()));
//        } else {
//            b.setFriendStatus(ru.sentidas.rangiffler.grpc.FriendStatus.NOT_FRIEND);
//        }
//        return b.build();
//    }
//
//    public static ru.sentidas.rangiffler.grpc.UsersPageResponse toProto(Page<UserService.UserWithComputedStatus> page) {
//        var pb = ru.sentidas.rangiffler.grpc.UsersPageResponse.newBuilder()
//                .setTotalElements(page.getTotalElements()) // int64 в proto — норм
//                .setTotalPages(page.getTotalPages())
//                .setFirst(page.isFirst())
//                .setLast(page.isLast())
//                .setPage(page.getNumber())
//                .setSize(page.getSize());
//
//        page.getContent().forEach(uw -> pb.addContent(toProto(uw)));
//        return pb.build();
//    }
//
//    public static ru.sentidas.rangiffler.grpc.UsersPageResponse toProto(Slice<UserService.UserWithComputedStatus> slice) {
//        // Если тебе нужен именно Slice -> собери page-поля вручную
//        var pb = ru.sentidas.rangiffler.grpc.UsersPageResponse.newBuilder()
//                .setTotalElements(0)           // нет в Slice — можно 0
//                .setTotalPages(0)              // нет в Slice — можно 0
//                .setFirst(slice.getNumber() == 0)
//                .setLast(!slice.hasNext())
//                .setPage(slice.getNumber())
//                .setSize(slice.getSize());
//        slice.forEach(uw -> pb.addContent(toProto(uw)));
//        return pb.build();
//    }
//}
