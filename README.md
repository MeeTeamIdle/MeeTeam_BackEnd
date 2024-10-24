# MeeTeam_BackEnd

# 아키텍처
<img width="619" alt="스크린샷 2024-06-30 오전 9 52 41" src="https://github.com/user-attachments/assets/2dfd9693-f571-4a8d-9c34-ecacb3818014">


# ERD(V1)
<img width="696" alt="스크린샷 2024-06-30 오전 12 24 30" src="https://github.com/MeeTeamNumdle/MeeTeam_BackEnd/assets/100754581/a6b72f8d-fc1c-4211-b3af-b1ca18aff962">


# 🌱 Branch

<aside>

`main branch` : 배포 단위 branch

`develop branch` : 주요 개발 branch, main merge 전 거치는 branch

`feature branch`: 각자 개발 branch

-   구현할 것들 별로 issue 생성
-   issue 번호를 기반으로 branch 생성
    -   ex) feat/#이슈번호
    

-   해당 branch 작업 완료 후 PR 보내기
    -   항상 local에서 충돌 해결 후 → remote에 올리기
    -   reviewer에 서로 tag후 code-review
    -   comment 전 merge 불가!

</aside>

<hr>

# 🙏 Commit Convention

`ex) [Feat] 댓글 작성 API 기능 구현`

```
- [CHORE]: 코드 수정, 내부 파일 수정
- [FEAT] : 새로운 기능 구현
- [ADD] : FEAT 이외의 부수적인 코드 추가, 라이브러리 추가, 새로운 파일 생성 시
- [FIX] : 버그, 오류 해결
- [DEL] : 쓸모없는 코드 삭제
- [DOCS] : README나 WIKI 등의 문서 개정
- [MOVE] : 프로젝트 내 파일이나 코드의 이동
- [RENAME] : 파일 이름의 변경
- [MERGE]: 다른브렌치를 merge하는 경우
- [STYLE] : 코드가 아닌 스타일 변경을 하는 경우
- [REFACTOR] : 로직은 변경 없는 클린 코드를 위한 코드 수정
```

# 🙏 Code Convention

> 💡 **동료들과 말투를 통일하기 위해 컨벤션을 지정합니다.**
> 오합지졸의 코드가 아닌, **한 사람이 짠 것같은 코드**를 작성하는 것이 추후 유지보수나 협업에서 도움이 됩니다. 내가 코드를 생각하면서 짤 수 있도록 해주는 룰이라고 생각해도 좋습니다!

<details>
<summary>명명규칙(Naming Conventions)</summary>
<div markdown="1">

1. 이름으로부터 의도가 읽혀질 수 있게 쓴다.

-   ex)

    ```jsx
    // bad
    function q() {
        // ...stuff...
    }

    // good
    function query() {
        // ..stuff..
    }
    ```

2. 오브젝트, 함수, 그리고 인스턴스에는 `camelCase`를 사용한다.

-   ex)
    ```jsx
    // bad
    const OBJEcttsssss = {};
    const this_is_my_object = {};
    function c() {}

    // good
    const thisIsMyObject = {};
    function thisIsMyFunction() {}
    ```

3. 클래스나 constructor에는 `PascalCase`를 사용한다.

-   ex)
    ```jsx
    // bad
    function user(options) {
        this.name = options.name;
    }

    const bad = new user({
        name: 'nope',
    });

    // good
    class User {
        constructor(options) {
            this.name = options.name;
        }
    }

    const good = new User({
        name: 'yup',
    });
    ```

4. 함수 이름은 동사 + 명사 형태로 작성한다.
   ex) `postUserInformation( )`
5. 약어 사용은 최대한 지양한다.
6. 이름에 네 단어 이상이 들어가면 팀원과 상의를 거친 후 사용한다
 </div>
 </details>

<details>
<summary>블록(Blocks)</summary>
<div markdown="1">

1. 복수행의 블록에는 중괄호({})를 사용한다.

-   ex)
    ```jsx
    // bad
    if (test)
      return false;

    // good
    if (test) return false;

    // good
    if (test) {
      return false;
    }

    // bad
    function() { return false; }

    // good
    function() {
      return false;
    }

    ```

2. 복수행 블록의 `if` 와 `else` 를 이용하는 경우 `else` 는 `if` 블록 끝의 중괄호( } )와 같은 행에 위치시킨다.

-   ex)
    ```java
    // bad
    if (test) {
    thing1();
    thing2();
    }
    else {
    thing3();
    }

    // good
    if (test) {
      thing1();
      thing2();
    } else {
      thing3();
    }

    ```
</div>
</details>

<details>
<summary>코멘트(Comments)</summary>
<div markdown="1">

1. 복수형의 코멘트는 `/** ... */` 를 사용한다.

-   ex)
    ```jsx
    // good
    /**
     * @param {String} tag
     * @return {Element} element
     */
    
    function make(tag) {
        // ...stuff...

        return element;
    }
    ```

2. 단일 행의 코멘트에는 `//` 을 사용하고 코멘트를 추가하고 싶은 코드의 상부에 배치한다. 그리고 코멘트의 앞에 빈 행을 넣는다.

-   ex)
    ```jsx
    // bad
    const active = true; // is current tab

    // good
    // is current tab
    const active = true;

    // good
    function getType() {
        console.log('fetching type...');

        // set the default type to 'no type'
        const type = this._type || 'no type';

        return type;
    }

    ```
</div>
</details>
