---
name: code-conventions
description: 코드 컨벤션 가이드 - Java 코드 작성 시 스타일 및 규칙 적용
alwaysApply: true
---

# 코드 컨벤션

## 1. Import 규칙

- 타입 참조 시 절대 경로(예: `java.util.List`)를 인라인으로 사용하지 않는다.
- 반드시 `import`문을 추가하고, 코드에서는 단순 클래스명으로 참조한다.

```java
// Bad
public void insertAttributes(java.util.List<MemberAttributeEntity> attributes) { }

// Good
import java.util.List;

public void insertAttributes(List<MemberAttributeEntity> attributes) { }
```
