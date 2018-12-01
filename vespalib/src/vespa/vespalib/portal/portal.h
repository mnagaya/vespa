// Copyright 2018 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include "listener.h"
#include "reactor.h"
#include "handle_manager.h"

#include <vespa/vespalib/net/crypto_engine.h>
#include <vespa/vespalib/net/crypto_socket.h>
#include <vespa/vespalib/stllike/string.h>

#include <map>
#include <memory>
#include <mutex>
#include <thread>

namespace vespalib {

namespace portal { class HttpConnection; }

/**
 * Minimal HTTP server and connection establishment manager.
 **/
class Portal
{
public:
    using SP = std::shared_ptr<Portal>;

    class Token {
        friend class Portal;
    private:
        Portal &_portal;
        uint64_t _handle;
        Token(const Token &) = delete;
        Token &operator=(const Token &) = delete;
        Token(Token &&) = delete;
        Token &operator=(Token &&) = delete;
        Token(Portal &portal, uint64_t handle)
            : _portal(portal), _handle(handle) {}
        uint64_t handle() const { return _handle; }
    public:
        using UP = std::unique_ptr<Token>;
        ~Token();
    };

    class GetRequest {
        friend class Portal;
    private:
        portal::HttpConnection *_conn;
        GetRequest(portal::HttpConnection &conn) : _conn(&conn) {}
    public:
        GetRequest(const GetRequest &rhs) = delete;
        GetRequest &operator=(const GetRequest &rhs) = delete;
        GetRequest &operator=(GetRequest &&rhs) = delete;
        GetRequest(GetRequest &&rhs) : _conn(rhs._conn) {
            rhs._conn = nullptr;
        }
        bool active() const { return (_conn != nullptr); }
        const vespalib::string &get_header(const vespalib::string &name) const;
        const vespalib::string &get_host() const;
        const vespalib::string &get_uri() const;
        void respond_with_content(const vespalib::string &content_type,
                                  const vespalib::string &content);
        void respond_with_error(int code, const vespalib::string &msg);
        ~GetRequest();
    };

    struct GetHandler {
        virtual void get(GetRequest request) const = 0;
        virtual ~GetHandler();
    };

private:
    struct BindState {
        uint64_t handle;
        vespalib::string prefix;
        const GetHandler *handler;
        BindState(uint64_t handle_in, vespalib::string prefix_in, const GetHandler &handler_in)
            : handle(handle_in), prefix(prefix_in), handler(&handler_in) {}
        bool operator<(const BindState &rhs) const {
            if (prefix.size() == rhs.prefix.size()) {
                return (handle > rhs.handle);
            }
            return (prefix.size() > rhs.prefix.size());
        }
    };

    CryptoEngine::SP       _crypto;
    portal::Reactor        _reactor;
    portal::HandleManager  _handle_manager;
    uint64_t               _conn_handle;
    portal::Listener::UP   _listener;
    std::mutex             _lock;
    std::vector<BindState> _bind_list;

    Token::UP make_token();
    void cancel_token(Token &token);

    portal::HandleGuard lookup_get_handler(const vespalib::string &uri, const GetHandler *&handler);
    void evict_handle(uint64_t handle);

    void handle_accept(portal::HandleGuard guard, SocketHandle socket);
    void handle_http(portal::HttpConnection *conn);

    Portal(CryptoEngine::SP crypto, int port);
public:
    ~Portal();
    static SP create(CryptoEngine::SP crypto, int port);
    int listen_port() const { return _listener->listen_port(); }
    Token::UP bind(const vespalib::string &path_prefix, const GetHandler &handler);
};

} // namespace vespalib