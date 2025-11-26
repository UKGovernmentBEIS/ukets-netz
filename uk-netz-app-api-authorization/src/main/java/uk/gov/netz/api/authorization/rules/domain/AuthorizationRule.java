package uk.gov.netz.api.authorization.rules.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;


/**
 * The AuthorizationRule Entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "au_rule")
@NamedQuery(
        name = AuthorizationRule.NAMED_QUERY_FIND_RULE_SCOPE_PERMISSIONS_BY_SERVICE_AND_ROLE_TYPE_AND_RESOURCE_TYPE_AND_RESOURCE_SUB_TYPE,
        query = "select distinct new uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission("
                + "rule.resourceType, rule.resourceSubType, rule.handler, ps.permission) "
                + "from AuthorizationRule rule "
                + "join rule.services service "
                + "left join ResourceScopePermission ps on ("
                + "		ps.resourceType = rule.resourceType "
                + "		and ps.roleType = rule.roleType "
                + "     and ((rule.resourceType is not null and ps.resourceType = rule.resourceType) or "
                + "     		(rule.resourceType is null and ps.resourceType is null)) "
                + "     and ((rule.resourceSubType is not null and ps.resourceSubType = rule.resourceSubType) or "
                + "     		(rule.resourceSubType is null and ps.resourceSubType is null)) "
                + "     and ps.scope = rule.scope) "
                + "where service.name = :serviceName "
                + "and rule.roleType = :roleType "
                + "and (:resourceType is null or rule.resourceType = :resourceType) "
                + "and (:resourceSubType is null or rule.resourceSubType = :resourceSubType)")

@NamedQuery(
        name = AuthorizationRule.NAMED_QUERY_FIND_RULE_SCOPE_PERMISSIONS_BY_RESOURCE_TYPE_SCOPE_AND_ROLE_TYPE,
        query = "select distinct new uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission("
                + "rule.resourceType, rule.resourceSubType, rule.handler, ps.permission) "
                + "from AuthorizationRule rule "
                + "left join ResourceScopePermission ps on ("
                + "		ps.resourceType = rule.resourceType "
                + "		and ps.roleType = rule.roleType "
                + "     and (rule.resourceSubType is not null and ps.resourceSubType = rule.resourceSubType) "
                + "     and ps.scope = rule.scope) "
                + "where rule.resourceType = :resourceType "
                + "and rule.scope = :scope "
                + "and rule.roleType = :roleType")
@NamedQuery(
        name = AuthorizationRule.NAMED_QUERY_FIND_RESOURCE_SUB_TYPE_ROLE_TYPES,
        query = "select distinct rule.resourceSubType as resourceSubType, rule.roleType as roleType "
                + "from AuthorizationRule rule "
                + "where rule.resourceSubType is not null")
@NamedQuery(
        name = AuthorizationRule.NAMED_QUERY_FIND_ROLE_TYPE_BY_RESOURCE_TYPE_AND_SUB_TYPE,
        query = "select distinct rule.roleType as roleType "
                + "from AuthorizationRule rule "
                + "where rule.resourceType = :resourceType and "
                + "rule.resourceSubType = :resourceSubType ")
@NamedQuery(
        name = AuthorizationRule.NAMED_QUERY_FIND_RESOURCE_SUB_TYPE_BY_RESOURCE_TYPE_AND_ROLE_TYPE,
        query = "select distinct rule.resourceSubType as resourceSubType "
                + "from AuthorizationRule rule "
                + "where rule.resourceType = :resourceType and "
                + "rule.roleType = :roleType ")
public class AuthorizationRule {

    public static final String NAMED_QUERY_FIND_RULE_SCOPE_PERMISSIONS_BY_SERVICE_AND_ROLE_TYPE_AND_RESOURCE_TYPE_AND_RESOURCE_SUB_TYPE =
            "AuthorizationRule.findRuleScopePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType";
    public static final String NAMED_QUERY_FIND_RULE_SCOPE_PERMISSIONS_BY_RESOURCE_TYPE_SCOPE_AND_ROLE_TYPE = "AuthorizationRule.findRuleScopePermissionsByResourceTypeScopeAndRoleType";
    public static final String NAMED_QUERY_FIND_RESOURCE_SUB_TYPE_ROLE_TYPES = "AuthorizationRule.findResourceSubTypeRoleTypes";
    public static final String NAMED_QUERY_FIND_ROLE_TYPE_BY_RESOURCE_TYPE_AND_SUB_TYPE = "AuthorizationRule.findRoleTypeByResourceTypeAndSubType";
    public static final String NAMED_QUERY_FIND_RESOURCE_SUB_TYPE_BY_RESOURCE_TYPE_AND_ROLE_TYPE = "AuthorizationRule.findResourceSubTypesByResourceTypeAndRoleType";

    /** The id. */
    @EqualsAndHashCode.Exclude
    @Id
    @SequenceGenerator(name = "au_rules_id_generator", sequenceName = "au_rule_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "au_rules_id_generator")
    private Long id;

    @NotNull
    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_sub_type")
    private String resourceSubType;

    @NotNull
    @Column(name = "handler")
    private String handler;

    @NotNull
    @Column(name = "role_type")
    private String roleType;

    @Column(name = "scope")
    private String scope;

    @Builder.Default
    @ManyToMany(mappedBy = "rules")
    private Set<AuthorizedService> services = new HashSet<>();
}
