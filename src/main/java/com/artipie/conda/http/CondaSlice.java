/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/conda-adapter/LICENSE
 */
package com.artipie.conda.http;

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.conda.http.auth.TokenAuth;
import com.artipie.conda.http.auth.TokenAuthScheme;
import com.artipie.conda.http.auth.TokenAuthSlice;
import com.artipie.http.Slice;
import com.artipie.http.auth.Action;
import com.artipie.http.auth.Authentication;
import com.artipie.http.auth.BasicAuthSlice;
import com.artipie.http.auth.Permission;
import com.artipie.http.auth.Permissions;
import com.artipie.http.auth.TokenAuthentication;
import com.artipie.http.auth.Tokens;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.StandardRs;
import com.artipie.http.rt.ByMethodsRule;
import com.artipie.http.rt.RtRule;
import com.artipie.http.rt.RtRulePath;
import com.artipie.http.rt.SliceRoute;
import com.artipie.http.slice.KeyFromPath;
import com.artipie.http.slice.SliceDownload;
import com.artipie.http.slice.SliceSimple;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main conda entry point.
 * @since 0.4
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.ExcessiveMethodLength"})
public final class CondaSlice extends Slice.Wrap {

    /**
     * Transform pattern for download slice.
     */
    private static final Pattern PTRN = Pattern.compile(".*/(.*/.*(\\.tar\\.bz2|\\.conda))$");

    /**
     * Anonymous tokens.
     */
    private static final Tokens ANONYMOUS = new Tokens() {
        @Override
        public TokenAuthentication auth() {
            return TokenAuth.ANONYMOUS;
        }

        @Override
        public String generate(final Authentication.User user) {
            return "abc123";
        }
    };

    /**
     * Ctor.
     * @param storage Storage
     * @param url Application url
     */
    public CondaSlice(final Storage storage, final String url) {
        this(storage, Permissions.FREE, Authentication.ANONYMOUS, CondaSlice.ANONYMOUS, url);
    }

    /**
     * Ctor.
     * @param storage Storage
     * @param perms Permissions
     * @param users Users
     * @param tokens Tokens
     * @param url Application url
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public CondaSlice(final Storage storage, final Permissions perms, final Authentication users,
        final Tokens tokens, final String url) {
        super(
            new SliceRoute(
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath("/t/.*repodata\\.json$"),
                        new ByMethodsRule(RqMethod.GET)
                    ),
                    new TokenAuthSlice(
                        new DownloadRepodataSlice(storage),
                        new Permission.ByName(perms, Action.Standard.READ), tokens.auth()
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(".*repodata\\.json$"),
                        new ByMethodsRule(RqMethod.GET)
                    ),
                    new BasicAuthSlice(
                        new DownloadRepodataSlice(storage), users,
                        new Permission.ByName(perms, Action.Standard.READ)
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath("(/dist/|/t/).*(\\.tar\\.bz2|\\.conda)$"),
                        new ByMethodsRule(RqMethod.GET)
                    ),
                    new TokenAuthSlice(
                        new SliceDownload(storage, CondaSlice.transform()),
                        new Permission.ByName(perms, Action.Standard.READ), tokens.auth()
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(".*(\\.tar\\.bz2|\\.conda)$"),
                        new ByMethodsRule(RqMethod.GET)
                    ),
                    new BasicAuthSlice(
                        new SliceDownload(storage, CondaSlice.transform()), users,
                        new Permission.ByName(perms, Action.Standard.READ)
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath("/(stage|commit).*(\\.tar\\.bz2|\\.conda)$"),
                        new ByMethodsRule(RqMethod.POST)
                    ),
                    new TokenAuthSlice(
                        new PostStageCommitSlice(url),
                        new Permission.ByName(perms, Action.Standard.READ), tokens.auth()
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(".*(\\.tar\\.bz2|\\.conda)$"),
                        new ByMethodsRule(RqMethod.POST)
                    ),
                    new TokenAuthSlice(
                        new UpdateSlice(storage),
                        new Permission.ByName(perms, Action.Standard.READ), tokens.auth()
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(".*(package|release).*"), new ByMethodsRule(RqMethod.GET)
                    ),
                    new TokenAuthSlice(
                        new GetPackageSlice(),
                        new Permission.ByName(perms, Action.Standard.READ), tokens.auth()
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(".*(package|release).*"), new ByMethodsRule(RqMethod.POST)
                    ),
                    new TokenAuthSlice(
                        new PostPackageReleaseSlice(),
                        new Permission.ByName(perms, Action.Standard.READ), tokens.auth()
                    )
                ),
                new RtRulePath(new ByMethodsRule(RqMethod.HEAD), new SliceSimple(StandardRs.OK)),
                new RtRulePath(
                    new RtRule.All(new RtRule.ByPath("/user"), new ByMethodsRule(RqMethod.GET)),
                    new TokenAuthSlice(
                        new GetUserSlice(new TokenAuthScheme(new TokenAuth(tokens.auth()))),
                        new Permission.ByName(perms, Action.Standard.READ),
                        tokens.auth()
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(".*authentication-type$"),
                        new ByMethodsRule(RqMethod.GET)
                    ),
                    new AuthTypeSlice()
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(".*authentications$"), new ByMethodsRule(RqMethod.POST)
                    ),
                    new BasicAuthSlice(
                        new GenerateTokenSlice(users, tokens), users,
                        new Permission.ByName(perms, Action.Standard.WRITE)
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(".*authentications$"), new ByMethodsRule(RqMethod.DELETE)
                    ),
                    new BasicAuthSlice(
                        new DeleteTokenSlice(tokens), users,
                        new Permission.ByName(perms, Action.Standard.WRITE)
                    )
                ),
                new RtRulePath(RtRule.FALLBACK, new SliceSimple(StandardRs.NOT_FOUND))
            )
        );
    }

    /**
     * Function to transform path to download conda package. Conda client can perform requests
     * for download with user token:
     * /t/user-token/linux-64/some-package.tar.bz2
     * @return Function to transform path to key
     */
    private static Function<String, Key> transform() {
        return path -> {
            final Matcher mtchr = PTRN.matcher(path);
            final Key res;
            if (mtchr.matches()) {
                res = new Key.From(mtchr.group(1));
            } else {
                res = new KeyFromPath(path);
            }
            return res;
        };
    }
}
