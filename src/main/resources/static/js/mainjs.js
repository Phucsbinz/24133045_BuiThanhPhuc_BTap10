/* Trang 32-34: gọi API bằng jQuery AJAX và Authorization Bearer. */
$(function () {
 const key = 'tuan10.jwt.token';
 function feedback(message, error = false) {
  $('#feedback').text(message).toggleClass('text-danger', error).toggleClass('text-success', !error);
 }
 function failure(xhr, authenticated = false) {
  feedback(xhr.responseJSON?.detail || 'Không thể kết nối máy chủ.', true);
  if (authenticated && (xhr.status === 401 || xhr.status === 403)) {
   localStorage.removeItem(key);
   window.location.replace('/login?expired=true');
  }
 }
 function api(url, method, data, protectedApi = false) {
  return $.ajax({url, type: method, dataType: 'json', contentType: 'application/json; charset=utf-8',
   data: data ? JSON.stringify(data) : undefined,
   beforeSend: function (xhr) {
    if (protectedApi) xhr.setRequestHeader('Authorization', 'Bearer ' + (localStorage.getItem(key) || ''));
   }});
 }
 $('#loginForm').on('submit', function (event) {
  event.preventDefault();
  api('/auth/login', 'POST', {email: $('#email').val(), password: $('#password').val()})
   .done(function (data) {localStorage.setItem(key, data.token); window.location.href = '/user/profile';})
   .fail(function (xhr) {failure(xhr);});
 });
 $('#signupForm').on('submit', function (event) {
  event.preventDefault();
  api('/auth/signup', 'POST', {fullName: $('#fullName').val(), email: $('#signupEmail').val(), password: $('#signupPassword').val()})
   .done(function (data) {
    $('#email').val(data.email); $('#signupPassword').val(''); feedback('Đăng ký thành công. Bạn có thể đăng nhập.');
   }).fail(function (xhr) {failure(xhr);});
 });
 $('#logout').on('click', function () {localStorage.removeItem(key); window.location.href = '/login';});
 $('#loadUsers').on('click', function () {
  api('/users', 'GET', null, true).done(function (users) {
   const body = $('#usersTable tbody').empty();
   users.forEach(function (user) {
    const row = $('<tr>'); [user.id, user.fullName, user.email].forEach(value => row.append($('<td>').text(value))); body.append(row);
   });
   $('#usersTable').prop('hidden', false);
  }).fail(function (xhr) {failure(xhr, true);});
 });
 if ($('#profile').length) {
  if (!localStorage.getItem(key)) {window.location.replace('/login'); return;}
  api('/users/me', 'GET', null, true).done(function (data) {
   $('#profile').text(data.fullName); $('#profileEmail').text(data.email);
   $('#images').attr('src', data.images || '/images/avatar.svg');
   $('#profileJson').text(JSON.stringify(data, null, 2));
  }).fail(function (xhr) {failure(xhr, true);});
 }
 if (new URLSearchParams(window.location.search).has('expired')) feedback('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.', true);
});

