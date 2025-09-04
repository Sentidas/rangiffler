package guru.qa.rangiffler.entity;

import guru.qa.rangiffler.model.FriendshipStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "friendship")
@IdClass(FriendShipId.class)
public class FriendshipEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "requester_id", referencedColumnName = "id")
    private UserEntity requester;

    @Id
    @ManyToOne
    @JoinColumn(name = "addressee_id", referencedColumnName = "id")
    private UserEntity addressee;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column
    @Enumerated(EnumType.STRING)
    FriendshipStatus status;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        FriendshipEntity that = (FriendshipEntity) o;
        return getRequester() != null && Objects.equals(getRequester(), that.getRequester())
                && getAddressee() != null && Objects.equals(getAddressee(), that.getAddressee())
                && getCreatedDate() != null && Objects.equals(getCreatedDate(), that.getCreatedDate());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(requester, addressee, createdDate);
    }
}
