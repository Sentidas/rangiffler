//package ru.sentidas.rangiffler.model;
//
//import guru.qa.rangiffler.entity.UserEntity;
//
//import java.util.Base64;
//import java.util.List;
//import java.util.UUID;
//
//public record User(
//        UUID id,
//        String username,
//        String firstname,
//        String surname,
//        String avatar,
//        FriendStatus friendStatus,
//        List<User> friends,
//        List<User> incomeInvitation,
//        List<User> outcomeInvitation,
//        Country location
//) {
//
//    public static User fromUserEntity(UserEntity userEntity, FriendStatus friendStatus) {
//
//        String avatarDataUrl = null;
//        if (userEntity.getAvatar() != null) {
//            // Конвертируем byte[] в Data URL
//            String base64Avatar = Base64.getEncoder().encodeToString(userEntity.getAvatar());
//            avatarDataUrl = "data:image/png;base64," + base64Avatar;
//        }
//
//        return new User(
//                userEntity.getId(),
//                userEntity.getUsername(),
//                userEntity.getFirstname(),
//                userEntity.getSurname(),
//                avatarDataUrl,
//                friendStatus,
//                null,
//                null,
//                null,
//                Country.fromEntity(userEntity.getCountry())
//        );
//    }
//
//    public static User fromUserEntity(UserEntity userEntity) {
//
//        String avatarDataUrl = null;
//        if (userEntity.getAvatar() != null) {
//            // Конвертируем byte[] в Data URL
//            String base64Avatar = Base64.getEncoder().encodeToString(userEntity.getAvatar());
//            avatarDataUrl = "data:image/png;base64," + base64Avatar;
//        }
//
//        return new User(
//                userEntity.getId(),
//                userEntity.getUsername(),
//                userEntity.getFirstname(),
//                userEntity.getSurname(),
//                avatarDataUrl,
//                null,
//                null,
//                null,
//                null,
//                Country.fromEntity(userEntity.getCountry())
//        );
//    }
//}
