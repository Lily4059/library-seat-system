export default [
  {
    url: '/api/login',
    method: 'post',
    response: ({ body }) => {
      const { username, password } = body
      if (username === 'test' && password === '123456') {
        return {
          code: 200,
          data: {
            token: 'fake-token',
            userInfo: { id: 1, name: '张三', credit: 100 }
          }
        }
      }
      return { code: 401, message: '用户名或密码错误' }
    }
  }
]